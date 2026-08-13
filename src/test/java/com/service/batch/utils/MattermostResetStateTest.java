package com.service.batch.utils;

import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.utils.enums.ChannelEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MattermostResetStateTest {

    @Test
    void sharesResetStateThroughTheDatabase() {
        ResetPointREP resetPointREP = mock(ResetPointREP.class);
        MattermostResetState state = new MattermostResetState(resetPointREP);
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue();
        when(resetPointREP.countByResetYnAndPointIdIn(eq("n"), anyCollection())).thenReturn(3L);

        assertThat(state.isResetting(channelId)).isTrue();
    }

    @Test
    void tracksTheChannelLocallyDuringReset() {
        MattermostResetState state = new MattermostResetState(mock(ResetPointREP.class));
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue();

        state.begin(channelId);
        assertThat(state.isResetting(channelId)).isTrue();

        state.end(channelId);
        assertThat(state.isResetting(channelId)).isFalse();
    }
}
