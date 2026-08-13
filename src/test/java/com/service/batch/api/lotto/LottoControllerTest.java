package com.service.batch.api.lotto;

import com.service.batch.config.InternalAuthFilter;
import com.service.batch.service.lotto.biz.LottoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class LottoControllerTest {

    @Test
    void returnsInternalServerErrorWhenPurchaseFails() throws Exception {
        LottoService lottoService = mock(LottoService.class);
        LottoController controller = new LottoController(lottoService);
        doThrow(new IllegalStateException("purchase failed")).when(lottoService).buy();
        MockMvc mockMvc = standaloneSetup(controller)
                .addFilters(new InternalAuthFilter())
                .build();

        mockMvc.perform(post("/lotto/api/buy").header("Auth-header", "second"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void healthEndpointRequiresInternalAuthHeader() throws Exception {
        LottoService lottoService = mock(LottoService.class);
        LottoController controller = new LottoController(lottoService);
        MockMvc mockMvc = standaloneSetup(controller)
                .addFilters(new InternalAuthFilter())
                .build();

        mockMvc.perform(get("/lotto/api/health"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/lotto/api/health").header("Auth-header", "second"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
