package org.unicefkidpower.schools.define;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.model.Language;

import java.util.ArrayList;
import java.util.Locale;

public class KPConstants {
    public static final String OLD_FLURRY_KEY = "JDBGDKT2DCTD3YBRHQV3";
    public static final String FLURRY_API_KEY = OLD_FLURRY_KEY;
    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_JSON_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final int CHECK_UPDATEVERSION_INTERVAL = 1 * 60; //minutes

    public static final int SCANNEDITEM_LEFTMARGIN_DELTA = 20;
    public static final int STUDENTNAME_MAXLENGTH = 12;

    public static final long CONFIGDEVICE_TIMEOUT = 3 * 60 * 60 * 1000;

    public static final String kUKPPolicyURL = "http://unicefkidpower.org/privacy";

    public static final ArrayList<Language> SUPPORTED_LANGUAGES = new ArrayList<Language>() {{
        add(new Language("English (US)", R.drawable.usa, new Locale(Locale.US.getLanguage(), Locale.US.getCountry())));
        add(new Language("English (UK)", R.drawable.uk, new Locale(Locale.UK.getLanguage(), Locale.UK.getCountry())));
        add(new Language("Dutch (NL)", R.drawable.netherlands, new Locale("nl", "NL")));
    }};

    public static final String SWRVE_BAND_LINK_STARTED = "band.link.started";
    public static final String SWRVE_BAND_LINK_SUCCESS = "band.link.success";
    public static final String SWRVE_BAND_LINK_ERROR = "band.link.error";
    public static final String SWRVE_BAND_UNLINKED = "band.unlinked";

    public static final String SWRVE_BAND_SYNC_SUCCESS = "band.sync.success";
    public static final String SWRVE_BAND_SYNC_ERROR = "band.sync.error";
    public static final String SWRVE_GROUP_SYNC_COMPLETE = "group.sync.complete";

    public static final String SWRVE_USER_APPLICATION_APPROVED = "user.application.approved";
    public static final String SWRVE_USER_ACCOUNT_VERIFIED = "user.account.verified";
    public static final String SWRVE_USER_ACCOUNT_UPDATED = "user.account.updated";

    public static final String SWRVE_TEAM_CREATED = "team.created";
    public static final String SWRVE_STUDENT_CREATED = "student.created";
    public static final String SWRVE_STUDENT_CREATED10 = "student.created.10_accounts";

    public static final String SWRVE_STUDENT_ICON_UPDATED = "student.icon.updated";
    public static final String SWRVE_STUDENT_DELETED = "student.deleted";

    public static final String SWRVE_LEADERBOARD_FOLLOW = "leaderboard.follow";
    public static final String SWRVE_LEADERBOARD_UNFOLLOW = "leaderboard.unfollow";
	public static final String HELP_CENTER_EMAIL			= "hello@unicefkidpower.org";

}
