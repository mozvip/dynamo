package dynamo.magazines;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.FileNameUtils;
import core.WebResource;
import dynamo.backlog.tasks.files.FileUtils;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.magazines.MagazineDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadSuggestion;
import dynamo.model.DownloadableStatus;
import dynamo.model.magazines.Magazine;
import dynamo.model.magazines.MagazineIssue;
import dynamo.parsers.MagazineIssueInfo;
import dynamo.parsers.MagazineNameParser;
import dynamo.webapps.googleimages.GoogleImages;

public class MagazineManager implements Reconfigurable {

	@Configurable(category="Magazines", name="Enable Magazines", bold=true)
	private boolean enabled;

	@Configurable(category="Magazines", name="Magazines Default Language", disabled="#{!MagazineManager.enabled}", required="#{MagazineManager.enabled}")
	private Language defaultLanguage = Language.EN;

	@Configurable(category="Magazines", name="Magazine Folders", contentsClass=Path.class, disabled="#{!MagazineManager.enabled}", required="#{MagazineManager.enabled}")
	private List<Path> folders;

	@Configurable(category="Magazines", name="Download Providers", contentsClass=MagazineProvider.class, disabled="#{!MagazineManager.enabled}", required="#{MagazineManager.enabled}", ordered=true)
	private List<MagazineProvider> providers;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<MagazineProvider> getProviders() {
		return providers;
	}

	public void setProviders(List<MagazineProvider> providers) {
		this.providers = providers;
	}

	public List<Path> getFolders() {
		return folders;
	}

	public void setFolders(List<Path> folders) {
		this.folders = folders;
	}
	
	public Language getDefaultLanguage() {
		return defaultLanguage;
	}
	
	public void setDefaultLanguage(Language defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
	
	private MagazineDAO magazineDAO = DAOManager.getInstance().getDAO( MagazineDAO.class );
	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );

	private MagazineManager() {
	}

	static class SingletonHolder {
		static MagazineManager instance = new MagazineManager();
	}

	public static MagazineManager getInstance() {
		return SingletonHolder.instance;
	}

	public Magazine findOrCreateMagazine( String magazineName, Language language, Path path ) {

		String searchName = magazineName.replaceAll("\\s", "");
		searchName = magazineName.replaceAll("[\\W]", "");
		searchName += "_" + (language != null ? language.getShortName() : "UNK");
		searchName = searchName.toUpperCase();

		Magazine m = magazineDAO.findBySearchName(searchName);
		if (m == null ) {
			
			String folderName = String.format( "%s (%s)", magazineName, language != null ? language.getLabel() : "Unknown Language" );
			Path magazinePath = path.resolve( FileNameUtils.sanitizeFileName(folderName) ).toAbsolutePath();

			List<String> aka = new ArrayList<>();
			aka.add( magazineName );

			m = new Magazine(magazineName, searchName, language, magazinePath, null, false, null, aka, null);
			magazineDAO.create(m.getSearchName(), m.getName(), m.getPath(), m.getAka(), m.getLanguage() );
			
		}
		return m;

	}

	@Override
	public void reconfigure() {
		if (!enabled) {
			return;
		}
	}
	

	public void setAutoDownload( String magazineSearchName, boolean autoDownload ) {

		// TODO Auto-generated method stub
	}

	public void subscribe(String magazineSearchName) {
		setAutoDownload( magazineSearchName, true );
	}

	public void unsubscribe(String magazineSearchName) {
		setAutoDownload( magazineSearchName, false );
	}
	
	public void deleteMagazine( String magazineSearchName ) {
		magazineDAO.deleteMagazine( magazineSearchName ); 
	}

	public List<Magazine> getMagazines() {
		return magazineDAO.findAll();
	}

	public Magazine find(String searchName) {
		return magazineDAO.findBySearchName(searchName);
	}

	public void save(Magazine magazine) {
		magazineDAO.save( magazine.getSearchName(), magazine.getPath(), magazine.getAka(), magazine.getWordsBlackList(), magazine.getLanguage() );
	}

	public List<MagazineIssue> getAllIssues(Magazine magazine) {
		return magazineDAO.findIssues( magazine.getSearchName() );
	}

	public void deleteKiosk() {
		downloadableDAO.delete( MagazineIssue.class, DownloadableStatus.SUGGESTED);
	}

	public List<MagazineIssue> getKioskContents( Language language, String filter ) {
		return magazineDAO.getKioskContents(language, filter );
	}

	public Magazine findOrCreateMagazine(String magazineName, Language language) {
		return findOrCreateMagazine(magazineName, language, FileUtils.getFolderWithMostUsableSpace(getFolders()));
	}
	
	public MagazineIssue createIssue( Magazine magazine, String rawIssueName, String coverImage ) {
		MagazineIssueInfo info = MagazineNameParser.getIssueInfo( rawIssueName );
		DownloadableStatus status = magazine.isAutoDownload() ? DownloadableStatus.WANTED : DownloadableStatus.IGNORED;
		MagazineIssue issue = new MagazineIssue(null, null, status, null, magazine.getSearchName(), magazine.getLanguage(), rawIssueName, info.getIssueDate(), info.getYear(), info.getIssueNumber(), info.isSpecial(), coverImage, new Date() );
		return issue;
	}

	public List<MagazineIssue> getCollectionContents( Language language, String filter ) {
		return magazineDAO.getCollectionContents( language, filter );
	}

	public List<MagazineIssue> getWantedContents(Language language, String filter) {
		return magazineDAO.getWantedContents( language, filter );
	}
	
	public synchronized void suggest( DownloadSuggestion suggestion ) {
		MagazineIssueInfo issueInfo = MagazineNameParser.getIssueInfo( suggestion.getTitle() );
		
		if ( issueInfo == null || issueInfo.getMagazineName() == null ) {
			ErrorManager.getInstance().reportWarning( String.format("Impossible to parse magazine issue info from : %s", suggestion.getTitle()), true); 
			return;
		}

		MagazineIssue existingIssue = null;
		Magazine magazine = MagazineManager.getInstance().findOrCreateMagazine(issueInfo.getMagazineName(), issueInfo.getLanguage());

		// check if this issue already exists
		List<MagazineIssue> existingIssuesForMagazine = magazineDAO.findIssues( magazine.getSearchName() );
		for (MagazineIssue magazineIssue : existingIssuesForMagazine) {
			
			if (magazineIssue.getIssueDate() != null && magazineIssue.getIssueDate().equals( issueInfo.getIssueDate() )) {
				existingIssue = magazineIssue;
				break;
			}
			
			if (magazineIssue.getIssueDate() == null && issueInfo.getIssueDate() == null && magazineIssue.getIssue() == issueInfo.getIssueNumber() ) {
				existingIssue = magazineIssue;
				break;
			}
		}

		String coverImage = null;
		if (suggestion.getImageURL() == null) {
			WebResource imageResource = GoogleImages.findImage(suggestion.getTitle(), 0.7f);
			if (imageResource != null) {
				coverImage = LocalImageCache.getInstance().download("magazines", suggestion.getTitle(), imageResource.getUrl(), imageResource.getReferer());
			}
		} else {
			coverImage = LocalImageCache.getInstance().download("magazines", suggestion.getTitle(), suggestion.getImageURL(), suggestion.getReferer());			
		}
		
		long downloadableId;
		String rawName = issueInfo.toString();
		if (existingIssue == null) {
			existingIssue = MagazineManager.getInstance().createIssue(magazine, issueInfo.getIssueName(), coverImage);
			downloadableId = DownloadableManager.getInstance().createDownloadable( MagazineIssue.class, rawName, null, coverImage, DownloadableStatus.SUGGESTED);
		} else {
			if (existingIssue.getCoverImage() == null && coverImage != null) {
				DownloadableManager.getInstance().updateCoverImage( existingIssue.getId(), coverImage);
			}
			downloadableId = existingIssue.getId();
		}
		
		int issueNumber = existingIssue.getIssue() > 0 ? existingIssue.getIssue() : issueInfo.getIssueNumber(); 

		magazineDAO.saveIssue(
				downloadableId, issueNumber, issueInfo.getIssueDate(),
				issueInfo.getYear(), issueInfo.isSpecial(), issueInfo.getLanguage() != null ? issueInfo.getLanguage() : suggestion.getLanguage(),
				magazine.getSearchName());

		DownloadableManager.getInstance().saveDownloadLocations(downloadableId, suggestion.getTitle(), suggestion.getSuggesterName(), suggestion.getDownloadFinderClass(), suggestion.getReferer(), suggestion.getSize(), suggestion.getDownloadLocations());
	}
	
}
