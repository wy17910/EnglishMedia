package ru.vinyarsky.englishmedia;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.util.AttributeSet;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlaybackControlView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vinyarsky.englishmedia.media.MediaService;

/**
 * Standard PlaybackControlView from ExpPlayer + podcast title view and episode title view
 */
public class EMPlaybackControlView extends PlaybackControlView {

    private MediaService.MediaServiceEventManager mediaServiceEventManager;
    private MediaService.MediaServiceListener mediaServiceListener = (podcastTitle, episodeTitle) -> {
        this.podcastTitleView.setText(podcastTitle);
        this.episodeTitleView.setText(episodeTitle);

        // No chosen episode? No annoying control view
        BottomSheetBehavior behavior = BottomSheetBehavior.from(this);
        if (episodeTitle == null) {
            hide();
            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setHideable(false);
            show();
        }
    };

    @BindView(R.id.textView_exo_playback_control_podcast_title)
    TextView podcastTitleView;

    @BindView(R.id.textView_exo_playback_control_episode_title)
    TextView episodeTitleView;

    public EMPlaybackControlView(Context context) {
        this(context, null);
    }

    public EMPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EMPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ButterKnife.bind(this, this);
        setShowTimeoutMs(-1);
    }

    public void setMediaServiceEventManager(MediaService.MediaServiceEventManager mediaServiceEventManager) {
        if (mediaServiceEventManager == this.mediaServiceEventManager) {
            return;
        }
        if (this.mediaServiceEventManager != null) {
            this.mediaServiceEventManager.removeListener(mediaServiceListener);
        }
        this.mediaServiceEventManager = mediaServiceEventManager;
        if (this.mediaServiceEventManager != null) {
            mediaServiceListener.onEpisodeChanged(this.mediaServiceEventManager.getPodcastTitle(), this.mediaServiceEventManager.getEpisodeTitle());
            this.mediaServiceEventManager.addListener(mediaServiceListener);
        }
    }
}