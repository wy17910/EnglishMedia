package ru.vinyarsky.englishmedia.media;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

import okhttp3.OkHttpClient;

@Module
public class MediaModule {

    @Provides
    @MediaScope
    public AudioFocus getAudioFocus(Context context) {
        return new AudioFocusImpl(context);
    }

    @Provides
    // @MediaScope - Player should be recreated every time MediaService starts
    public Player getPlayer(Context context, AudioFocus audioFocus, OkHttpClient httpClient) {
        return new PlayerImpl(context, audioFocus, httpClient);
    }
}
