package androidx.media3.exoplayer.text;
import androidx.media3.common.Format;
import androidx.media3.extractor.text.DefaultSubtitleParserFactory;
import androidx.media3.extractor.text.SubtitleParser;
public final class SubtitleTranscriptParserFactory implements SubtitleParser.Factory {
    private final SubtitleParser.Factory delegate = new DefaultSubtitleParserFactory();
    public SubtitleTranscriptParserFactory(SubtitleTranscript transcript) {}
    @Override public boolean supportsFormat(Format format) { return delegate.supportsFormat(format); }
    @Override public int getCueReplacementBehavior(Format format) { return delegate.getCueReplacementBehavior(format); }
    @Override public SubtitleParser create(Format format) { return delegate.create(format); }
}
