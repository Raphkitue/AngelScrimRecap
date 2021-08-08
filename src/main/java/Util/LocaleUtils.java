package Util;

import app.DependenciesContainer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Install;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;

public class LocaleUtils
{
    private static final Logger log = Loggers.getLogger(LocaleUtils.class);

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();

    private static final Map<String, ResourceBundle> locales = new HashMap<>();

    static {
        locales.put("de", ResourceBundle.getBundle("bottext", Locale.GERMAN));
        locales.put("fr", ResourceBundle.getBundle("bottext", Locale.FRENCH));
        locales.put("en", ResourceBundle.getBundle("bottext", Locale.ENGLISH));
    }

    public static String getLocaleString(String serverId, String id, String ...additions)
    {
       return getLocaleString(installsRepo.getInstallForServer(serverId), id, additions);
    }

    public static String getLocaleString(Install install, String id, String ...additions)
    {
        String lang;
        if (install != null)
            lang = install.getLang();
        else
            lang = "en";
        return getDirectLocaleString(lang, id, additions);
    }

    public static String getDirectLocaleString(String lang, String id, String ...additions)
    {
        String localized = locales.get(lang).getString(id);

        Pattern pattern = Pattern.compile("\\{}");
        Matcher matcher = pattern.matcher(localized);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        int j = 0;
        while (matcher.find()) {
            if(additions == null || j >= additions.length)
                return builder.toString();

            builder.append(localized, i, matcher.start());
            if (additions[j] == null)
                builder.append(matcher.group(0));
            else
                builder.append(additions[j]);
            i = matcher.end();
            j += 1;
        }
        builder.append(localized.substring(i));
        return builder.toString();
    }

}
