package com.nooblol.board.service;

import com.nooblol.board.dto.ArticleDto;
import javax.servlet.http.HttpSession;

public interface ArticleService {

    /**
     * 받은 articleId로 조회수를 증가한 이후 게시물정보를 반환한다.
     *
     * @param articleId 조회해야 하는 게시물 ID
     * @param userId 로그인을 한 경우, Session에 저장된 UserId
     * @return
     */
    ArticleDto getArticleInfo(int articleId, String userId);

    /**
     * 해당 사용자가 실제 사용자인지확인후, 게시물에서 행동할 수 있는 권한을 Return한다
     *
     * @param userId
     * @return
     */
    String getUserArticleAuth(String userId);

    /**
     * 받은 articleId의 조회수를 1 증가시킨다
     *
     * @param articleId
     */
    void addReadCount(int articleId);

    /**
     * 게시물 삽입
     *
     * @param articleDto
     * @return
     */
    boolean insertArticle(ArticleDto articleDto);

    /**
     * 게시물의 삽입 또는 수정을 진행한다.
     *
     * @param articleDto
     * @param session
     * @return
     */
    boolean updateArticle(ArticleDto articleDto, HttpSession session);

    /**
     * 게시물 삭제, 요청자가 관리자인 경우 또는 글 작성자인 경우에만 삭제를 진행한다. Delete를 진행하는 경우 관련 테이블에 대해서도 삭제가 이뤄지다 보니,
     * Transaction으로 묶어 처리를 진행한다
     *
     * @param articleId
     * @param session
     * @return
     */
    boolean deleteArticle(int articleId, HttpSession session);

    /**
     * DB에 실제 해당 게시물이 존재하는지 여부 확인
     *
     * @param articleId
     * @return
     */
    void checkNotExistsArticleByArticleId(int articleId);
}
