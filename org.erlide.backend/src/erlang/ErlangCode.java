package erlang;

import org.erlide.backend.BackendException;
import org.erlide.backend.ErlCallable;
import org.erlide.jinterface.util.ErlLogger;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;

public final class ErlangCode {

    private ErlangCode() {
    }

    public static void addPathA(final ErlCallable backend, final String path) {
        try {
            backend.call("code", "add_patha", "s", path);
        } catch (final Exception e) {
            ErlLogger.debug(e);
        }
    }

    public static void addPathZ(final ErlCallable backend, final String path) {
        try {
            backend.call("code", "add_pathz", "s", path);
        } catch (final Exception e) {
            ErlLogger.debug(e);
        }
    }

    public static void removePath(final ErlCallable backend, String path) {
        try {
            // workaround for bug in code:del_path
            try {
                final OtpErlangObject rr = backend.call("filename", "join",
                        "x", new OtpErlangList(new OtpErlangString(path)));
                path = ((OtpErlangString) rr).stringValue();
            } catch (final Exception e) {
                // ignore
            }
            backend.call("code", "del_path", "s", path);
        } catch (final Exception e) {
            ErlLogger.debug(e);
        }
    }

    public static OtpErlangObject loadBinary(final ErlCallable b,
            final String beamf, final OtpErlangBinary code)
            throws BackendException {
        OtpErlangObject result;
        result = b.call("code", "load_binary", "asb", beamf, beamf, code);
        return result;
    }

    public static void delete(final ErlCallable fBackend, final String moduleName) {
        try {
            fBackend.call("code", "delete", "a", moduleName);
        } catch (final Exception e) {
            ErlLogger.debug(e);
        }
    }

    public static void load(final ErlCallable backend, String name) {
        if (name.endsWith(".beam")) {
            name = name.substring(0, name.length() - 5);
        }
        try {
            backend.call("c", "l", "a", name);
        } catch (final Exception e) {
            ErlLogger.debug(e);
        }
    }

}
