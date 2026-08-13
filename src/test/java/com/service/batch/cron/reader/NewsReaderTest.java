package com.service.batch.cron.reader;

import com.service.batch.database.crawling.repository.NewsREP;
import com.service.batch.database.crawling.repository.NewsSubscribeEntityREP;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.NaverApiUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class NewsReaderTest {
    @Mock
    private NewsREP newsREP;
    @Mock
    private NaverApiUtil naverApiUtil;
    @Mock
    private NewsSubscribeEntityREP newsSubscribeEntityREP;
    @Mock
    private MattermostUtil mattermostUtil;
    @Mock
    private RestTemplate restTemplate;

    private NewsReader newsReader;

    @BeforeEach
    void setUp() {
        newsReader = new NewsReader(newsREP, naverApiUtil, newsSubscribeEntityREP, mattermostUtil, restTemplate);
    }

    @Test
    void findsExistingLinksOnceAndReturnsOnlyNewLinks() {
        Set<String> candidates = Set.of("https://old", "https://new-1", "https://new-2");
        when(newsREP.findExistingLinks(argThat(links -> links.containsAll(candidates))))
                .thenReturn(List.of("https://old"));

        Set<String> result = newsReader.findNewLinks(candidates);

        assertThat(result).containsExactlyInAnyOrder("https://new-1", "https://new-2");
        verify(newsREP).findExistingLinks(argThat(links -> links.size() == 3 && links.containsAll(candidates)));
    }

    @Test
    void doesNotQueryWhenThereAreNoCandidates() {
        assertThat(newsReader.findNewLinks(Set.of())).isEmpty();
        verify(newsREP, never()).findExistingLinks(Set.of());
    }
}
