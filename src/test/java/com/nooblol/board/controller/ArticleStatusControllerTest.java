package com.nooblol.board.controller;

import static org.mockito.BDDMockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nooblol.board.dto.LikeAndNotLikeResponseDto;
import com.nooblol.board.service.ArticleService;
import com.nooblol.board.service.ArticleStatusService;
import com.nooblol.global.utils.RestDocConfiguration;
import com.nooblol.global.utils.SessionSampleObject;
import javax.servlet.http.HttpSession;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleStatusController.class)
@Import(RestDocConfiguration.class)
@AutoConfigureRestDocs
class ArticleStatusControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ArticleService articleService;
    @MockBean ArticleStatusService articleStatusService;

    @Autowired ObjectMapper objectMapper;

    @Nested
    @DisplayName("게시물 추천, 비추천 테스트 케이스")
    class ArticleStatusTest {

        @Test
        @DisplayName("게시물의 추천과 비추천의 갯수를 조회할 때, articleId가 존재하는 경우 각각 두개의 상태에 대한 갯수를 획득한다.")
        void getLikeAndNotLikeArticle_WhenIsExistsArticleId_ThenReturnLikeAndNotLikeResponseDto()
                throws Exception {
            // given
            int articleId = 1;
            LikeAndNotLikeResponseDto likeAndNotLikeResponseDto =
                    new LikeAndNotLikeResponseDto().builder().likeCnt(5).notLikeCnt(4).build();

            // mock
            when(articleStatusService.likeAndNotListStatus(articleId))
                    .thenReturn(likeAndNotLikeResponseDto);

            // when & then
            mockMvc
                    .perform(RestDocumentationRequestBuilders.get("/article/status/{articleId}", articleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode", Is.is(HttpStatus.OK.value())))
                    .andDo(
                            document(
                                    "article/status/getLikeAndNotLike",
                                    pathParameters(
                                            parameterWithName("articleId").description("존재하는 Article(게시물) ID")),
                                    responseHeaders(
                                            headerWithName(HttpHeaders.CONTENT_TYPE)
                                                    .description(MediaType.APPLICATION_JSON_VALUE)),
                                    responseFields(
                                            fieldWithPath("resultCode").type(int.class).description("실행 결과의 상태값"),
                                            fieldWithPath("result.likeCnt")
                                                    .type(int.class)
                                                    .description("해당 Article의 추천 수"),
                                            fieldWithPath("result.notLikeCnt")
                                                    .type(int.class)
                                                    .description("해당 Article의 비추천 수"))));
        }

        @Test
        @DisplayName("게시물에 대한 추천을 할 경우, 이전에 추천 또는 비추천에 대한 기록이 없는 경우, Ok상태값과 true를 결과값으로 획득한다")
        void likeArticle_WhenIsNotExistsLikeOrNotLikeHistory_ThenReturnOkAndTrue() throws Exception {
            // given
            int articleId = 1;
            MockHttpSession session = (MockHttpSession) SessionSampleObject.authUserLoginSession;

            // mock
            when(articleStatusService.likeArticle(articleId, (HttpSession) session)).thenReturn(true);

            // when & then
            mockMvc
                    .perform(
                            RestDocumentationRequestBuilders.post("/article/status/like/{articleId}", articleId)
                                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode", Is.is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.result", Is.is(true)))
                    .andDo(getStatusDocument("article/status/like"));
        }

        @Test
        @DisplayName("게시물에 대한 비추천을 할 경우, 이전에 추천 또는 비추천에 대한 기록이 없는 경우, Ok상태값과 true를 결과값으로 획득한다")
        void NotLikeArticle_WhenIsNotExistsLikeOrNotLikeHistory_ThenReturnOkAndTrue() throws Exception {
            // given
            int articleId = 1;
            MockHttpSession session = (MockHttpSession) SessionSampleObject.authUserLoginSession;

            // mock
            when(articleStatusService.notLikeArticle(articleId, (HttpSession) session)).thenReturn(true);

            // when & then
            mockMvc
                    .perform(
                            RestDocumentationRequestBuilders.post(
                                            "/article/status/notLike/{articleId}", articleId)
                                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode", Is.is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.result", Is.is(true)))
                    .andDo(getStatusDocument("article/status/notLike"));
        }

        private RestDocumentationResultHandler getStatusDocument(String docsPathValue) {
            return document(
                    docsPathValue,
                    pathParameters(parameterWithName("articleId").description("존재하는 Article(게시물) ID")),
                    responseHeaders(
                            headerWithName(HttpHeaders.CONTENT_TYPE)
                                    .description(MediaType.APPLICATION_JSON_VALUE)),
                    responseFields(
                            fieldWithPath("resultCode").type(int.class).description("실행 결과의 상태값"),
                            fieldWithPath("result").type(boolean.class).description("실행 성공 유무")));
        }
    }
}
