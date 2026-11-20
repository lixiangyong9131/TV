package androidx.media3.exoplayer.libass;
import java.io.File;
import java.io.IOException;
public final class LibassFontFile {
    public static String getFamilyName(File file) throws IOException { return file == null ? null : file.getName(); }
}
