package com.service.batch.service.reset.api.biz;

import com.service.batch.database.batch.MySqlNamedLock;
import com.service.batch.database.batch.entity.ResetPointEntity;
import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.database.crawling.repository.MattermostSentREP;
import com.service.batch.utils.MattermostResetState;
import com.service.batch.utils.MattermostUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetImplTest {
    @Mock
    private ResetPointREP resetPointREP;
    @Mock
    private MattermostSentREP mattermostSentREP;
    @Mock
    private MattermostUtil mattermostUtil;
    @Mock
    private MattermostResetState mattermostResetState;
    @Mock
    private MySqlNamedLock mySqlNamedLock;
    @InjectMocks
    private ResetImpl reset;

    @Test
    void resetsWithoutHoldingOneTransactionAcrossMattermostCalls() throws Exception {
        List<ResetPointEntity> points = List.of(point(), point(), point());
        when(resetPointREP.findByResetYnAndPointIdInOrderByCreateDateDesc(eq("n"), anyCollection()))
                .thenReturn(points);
        when(mattermostUtil.selectAllChannel(anyString())).thenReturn(ResponseEntity.ok().build());
        when(mySqlNamedLock.runIfAcquired(eq("service-batch:mattermost-reset"), any())).thenAnswer(invocation -> {
            Callable<?> action = invocation.getArgument(1);
            action.call();
            return true;
        });

        reset.mattermostDelReset();

        verify(mattermostSentREP).deleteAllByCategoryIn(List.of("news", "coin", "hotdeal"));
        verify(resetPointREP).saveAll(points);
        assertThat(points).allMatch(point -> "y".equals(point.getResetYn()));
    }

    @Test
    void skipsWhenAnotherInstanceOwnsTheResetLock() throws Exception {
        when(mySqlNamedLock.runIfAcquired(eq("service-batch:mattermost-reset"), any())).thenReturn(false);

        reset.mattermostDelReset();

        verify(resetPointREP, never()).findByResetYnAndPointIdInOrderByCreateDateDesc(anyString(), anyCollection());
    }

    private static ResetPointEntity point() {
        return ResetPointEntity.builder().pointId(1).resetYn("n").build();
    }
}
