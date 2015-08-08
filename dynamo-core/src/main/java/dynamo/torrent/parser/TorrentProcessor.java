/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package dynamo.torrent.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import dynamo.core.manager.ErrorManager;

/**
 *
 * Class enabling to process a torrent file
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class TorrentProcessor {
    
    public static TorrentFile getTorrentFile( InputStream input ) {
    	return getTorrentFile( parseTorrent( input ));
    }

    /**
     * Given a File (supposed to be a torrent), parse it and represent it as a Map
     * @param file File
     * @return Map
     */
    private static Map parseTorrent( InputStream input ){
    	byte[] data;
		try {
			data = IOUtils.toByteArray(input);
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
		
		Map torrent = null;
		
        try{
        	torrent = BDecoder.decode( data );
        } catch(IOException e){
        	ErrorManager.getInstance().reportThrowable( e );
        }
        return torrent;
    }

    /**
     * Given a Map, retrieve all useful information and represent it as a TorrentFile object
     * @param m Map
     * @return TorrentFile
     */
    private static TorrentFile getTorrentFile(Map m) {
        if(m == null)
            return null;
        
        TorrentFile torrent = new TorrentFile();
        
        if(m.containsKey("announce")) // mandatory key
            torrent.announceURL = new String((byte[]) m.get("announce"));
        else
            return null;
        if(m.containsKey("comment")) // optional key
            torrent.comment = new String((byte[]) m.get("comment"));
        if(m.containsKey("created by")) // optional key
            torrent.createdBy = new String((byte[]) m.get("created by"));
        if(m.containsKey("creation date")) // optional key
            torrent.creationDate = (Long) m.get("creation date");
        if(m.containsKey("encoding")) // optional key
            torrent.encoding = new String((byte[]) m.get("encoding"));

        //Store the info field data
        if(!m.containsKey("info")) {
        	return null;
        }
        
        Map info = (Map) m.get("info");
        try{

            torrent.info_hash_as_binary = TorrentUtils.hash(BEncoder.encode(info));
            torrent.info_hash_as_hex = TorrentUtils.byteArrayToByteString(
                                            torrent.info_hash_as_binary);
            torrent.info_hash_as_url = TorrentUtils.byteArrayToURLString(
                                            torrent.info_hash_as_binary);
        }catch(IOException ioe){return null;}
        if (info.containsKey("name"))
            torrent.saveAs = new String((byte[]) info.get("name"));
        if (info.containsKey("piece length"))
            torrent.pieceLength = ((Long) info.get("piece length")).intValue();
        else
            return null;

        if (info.containsKey("pieces")) {
            byte[] piecesHash2 = (byte[]) info.get("pieces");
            if (piecesHash2.length % 20 != 0)
                return null;

            for (int i = 0; i < piecesHash2.length / 20; i++) {
                byte[] temp = TorrentUtils.subArray(piecesHash2, i * 20, 20);
                torrent.piece_hash_values_as_binary.add(temp);
                torrent.piece_hash_values_as_hex.add(TorrentUtils.
                        byteArrayToByteString(
                                temp));
                torrent.piece_hash_values_as_url.add(TorrentUtils.
                        byteArrayToURLString(
                                temp));
            }
        } else
            return null;

        if (info.containsKey("files")) {
            List<Map> multFiles = (List<Map>) info.get("files");
            torrent.total_length = 0;
            for (int i = 0; i < multFiles.size(); i++) {
            	
            	Map currentFile = multFiles.get(i);
            	
            	Long length = (Long) currentFile.get("length");
            	
                torrent.length.add( length );
                torrent.total_length += length.intValue();

                List path = (List) currentFile.get( "path" );
                String filePath = "";
                for (int j = 0; j < path.size(); j++) {
                	if (StringUtils.isNotEmpty( filePath )) {
                		filePath += "/";
                	}
                    filePath += new String((byte[]) path.get(j));
                }
                torrent.fileNames.add(filePath);
            }
        } else {
            torrent.length.add((Long)info.get("length"));
            torrent.total_length = (Long)info.get("length");
            torrent.fileNames.add(new String((byte[]) info.get("name")));
        }

        return torrent;
    }

}