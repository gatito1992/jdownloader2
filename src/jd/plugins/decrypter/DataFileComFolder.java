//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "datafile.com" }, urls = { "http://(www\\.)?datafile.com/f/[A-Za-z0-9]+" }, flags = { 0 })
public class DataFileComFolder extends PluginForDecrypt {

    public DataFileComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML("class=\"error\\-msg\"")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        final String[] links = br.getRegex("\"(https?://(www\\.)datafile\\.com/d/[A-Za-z0-9]+)\"").getColumn(0);
        if (links == null || links.length == 0) {
            if (br.containsHTML("class=\"file\\-size\"")) {
                logger.info("Link offline (folder empty): " + parameter);
                return decryptedLinks;
            }
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (final String singleLink : links)
            decryptedLinks.add(createDownloadlink(singleLink));

        return decryptedLinks;
    }

}
