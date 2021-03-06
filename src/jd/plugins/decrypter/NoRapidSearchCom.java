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
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "norapidsearch.com" }, urls = { "http://(www\\.)?norapidsearch\\.com/no\\-rapid\\-id/[a-z0-9]+" }, flags = { 0 })
public class NoRapidSearchCom extends PluginForDecrypt {

    public NoRapidSearchCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    // Uses same script as AllMscFindCom
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(false);
        br.getPage(parameter);
        if (br.containsHTML(">File was removed from filehosting|>File was removed by")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String fpName = br.getRegex("class=\"block\\-head\"><div class=\"block\\-head\\-inner\"><h1>Fast download of ([^<>\"\\']+) from <b>").getMatch(0);
        if (fpName == null) {
            fpName = br.getRegex("<title>Start to download ([^<>\"\\']+) file from .*?</title>").getMatch(0);
        }
        String postID = br.getRegex("type=\"hidden\" name=\"file_id\" value=\"(\\d+)\"").getMatch(0);
        if (postID == null) postID = br.getRegex("action=\"/go/(\\d+)\"").getMatch(0);
        if (postID == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        final String captchaLink = br.getRegex("\"(/captcha/\\d+)\"").getMatch(0);
        if (captchaLink == null && br.containsHTML("class=\"captcha\"")) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        if (captchaLink != null) {
            boolean failed = true;
            for (int i = 0; i <= 3; i++) {
                final String code = getCaptchaCode("http://www.norapidsearch.com" + captchaLink, param);
                br.postPage("http://www.norapidsearch.com/go", "file_id=" + postID + "&captcha=" + Encoding.urlEncode(code));
                if (br.getRedirectLocation() != null && br.getRedirectLocation().contains("/no-rapid-id/")) {
                    br.getPage(parameter);
                    continue;
                }
                failed = false;
                break;
            }
            if (failed) throw new DecrypterException(DecrypterException.CAPTCHA);
        } else {
            br.postPage("http://www.norapidsearch.com/go/" + postID, "file_id=" + postID);
        }
        final String finallink = br.getRedirectLocation();
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        if (!this.canHandle(finallink)) decryptedLinks.add(createDownloadlink(finallink));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return true;
    }

}