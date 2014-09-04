package tools.mikandi.dev.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public final class LoginStorageUtils {
	private static final String sName = "kb-login";
	private static final String sLogin = "login";
	private static final String sCount = "count";
	private static final String sUid = "uid";
	private static final String sUAHash = "user-auth-hash";
	private static final String sUAExpires = "user-auth-expires";
	private static final String sUADisplayName = "user-display-name";

	public static boolean isLoggedIn(final Context ctx) {
		final SharedPreferences sp = ctx.getSharedPreferences(sName, 0);
		if (!sp.contains(sLogin)) {
			return false;
		}
		if (Long.parseLong(sp.getString(sUAExpires, "0"), 10) < (System
				.currentTimeMillis() / 1000)) {
			Log.w("MiKandiBilling", "Login session expired");
			clear(ctx);
			return false;
		}
		return true;
	}

	public static void clear(final Context ctx) {
		ctx.getSharedPreferences(sName, 0).edit().clear().commit();
	}

	public static void setLogin(Context ctx, final LoginResult lr) {
		final SharedPreferences.Editor ed = ctx.getSharedPreferences(sName, 0).edit();
		ed.putBoolean(sLogin, true);
		ed.putInt(sUid, lr.mUserId);
		ed.putString(sUAExpires, lr.getUserAuthExpires());
		ed.putString(sUAHash, lr.getUserAuthHash());
		ed.putString(sUADisplayName, lr.getDisplayName());
		final String[] tokens = lr.getTokens();
		final int tlen = tokens.length;
		ed.putInt(sCount, tlen);
		for (int i = 0; i < tlen; i += 1) {
			ed.putString(sCount + String.valueOf(i), tokens[i]);
		}
		ed.commit();
	}

	public static LoginResult getLogin(Context ctx) {
		if (!LoginStorageUtils.isLoggedIn(ctx)) {
			return null;
		}
		final SharedPreferences sp = ctx.getSharedPreferences(sName, 0);
		final int tlen = sp.getInt(sCount, 0);
		String[] tokens = null;
		if (tlen > 1) {
			tokens = new String[tlen];
			for (int i = 0; i < tlen; i += 1) {
				tokens[i] = sp.getString(sCount + String.valueOf(i), null);
			}
		}
		return new LoginResult(LoginResult.RESULT_LOGIN_SUCCESS, sp.getInt(
				sUid, -1), tokens, sp.getString(sUAHash, null), sp.getString(
				sUAExpires, null), sp.getString(sUADisplayName, "MiKandi User"));
	}
	
}