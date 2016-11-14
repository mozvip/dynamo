package dynamo.subtitles.opensubtitles;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.manager.ErrorManager;

public class OpenSubtitlesOrg extends SubtitlesFinder {

	private XmlRpcClient xmlRPCClient = null;

	public OpenSubtitlesOrg() throws MalformedURLException {
		String url = "http://api.opensubtitles.org/xml-rpc";
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(url));
		xmlRPCClient = new XmlRpcClient();
		xmlRPCClient.setConfig(config);
	}

	@Override
	public FinderQuality getQuality() {
		return FinderQuality.PERFECT;
	}
	
	public enum OsLanguage {
		ENG( Language.EN, "eng" ),
		FRE( Language.FR, "fre" );
		
		private Language language;
		private String idSubLanguage;
		
		private OsLanguage( Language language, String idSubLanguage ) {
			this.language = language;
			this.idSubLanguage = idSubLanguage;
		}
		
		public Language getLanguage() {
			return language;
		}
		
		public String getIdSubLanguage() {
			return idSubLanguage;
		}
		
		public static OsLanguage find( Language language ) {
			for (OsLanguage osLang : OsLanguage.values()) {
				if (osLang.language == language) {
					return osLang;
				}
			}
			return null;
		}
	}

	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language) throws Exception {
		
		OsLanguage lang = OsLanguage.find(language);
		if ( lang == null ) {
			ErrorManager.getInstance().reportWarning( String.format("Language %s is not supported by the opensubtitles.org subtitles finder", language.getLabel() ));
			return null;
		}

		String hash = details.getOpenSubtitlesHash();
		if (hash == null) {
			return null;
		}

		String userAgent = "OS Test User Agent";

		Map<String, Object> result;
		try {
			result = (Map<String, Object>) xmlRPCClient.execute( "LogIn", new Object[] { "", "", language.getShortName(), userAgent });
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
		String token = (String) result.get("token");
		try {

			Map<String, String> sub = new HashMap<String, String>();
			

			sub.put("sublanguageid", lang.getIdSubLanguage() );
			sub.put("moviehash", hash);
			sub.put("moviebytesize", "" + Files.size( details.getPathToVideoFile() ));

			List<Map<String, String>> subtitles = new ArrayList<Map<String, String>>();

			subtitles.add(sub);

			result = (Map<String, Object>) xmlRPCClient.execute("SearchSubtitles", new Object[] { token, subtitles });
			Object data = result.get("data");

			if (data instanceof Object[]) {
				Object[] results = (Object[]) data;
				for (Object subtitle : results) {
					String downloadLink = (String) ((Map) subtitle).get("SubDownloadLink");
					String subtitlesLink = (String) ((Map) subtitle).get("SubtitlesLink");

					byte[] subTitleData = client.get( downloadLink, subtitlesLink ).getByteContents();
					return new RemoteSubTitles( subTitleData, downloadLink, 20);
				}
			}

		} finally {
			xmlRPCClient.execute("LogOut", new Object[] { token });
		}

		return null;
	}

}
