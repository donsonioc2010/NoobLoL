package com.nooblol.user.service.impl;

import com.nooblol.global.exception.ExceptionMessage;
import com.nooblol.global.utils.SessionUtils;
import com.nooblol.user.dto.LetterDto;
import com.nooblol.user.dto.LetterInsertRequestDto;
import com.nooblol.user.dto.LetterSearchDto;
import com.nooblol.user.mapper.LetterMapper;
import com.nooblol.user.service.LetterService;
import com.nooblol.user.service.UserInfoService;
import com.nooblol.user.utils.LetterStatus;
import com.nooblol.user.utils.LetterType;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService {

    private final LetterMapper letterMapper;

    private final UserInfoService userInfoService;

    @Override
    public LetterDto getLetter(int letterId, HttpSession session) {
        LetterDto letterDto = letterMapper.selectLetterByLetterId(letterId);
        String letterReqUserId = SessionUtils.getSessionUserId(session);

        if (ObjectUtils.isEmpty(letterDto)) {
            throw new IllegalArgumentException(ExceptionMessage.NO_DATA);
        }

        if (!isHaveGetLetterAuth(letterDto, letterReqUserId)) {
            throw new IllegalArgumentException(ExceptionMessage.FORBIDDEN);
        }

        // 발송자인 경우에는 먼저 return
        if (isReqUserLetterTypeFrom(letterDto, letterReqUserId)) {
            return letterDto;
        }
        // 수신자의 경우 읽지 않았으면 Update
        if (LetterStatus.UNREAD.equals(letterDto.getToStatus())) {
            letterDto.setToStatus(LetterStatus.READ);
            updateLetterStatus(letterDto.getLetterId(), LetterStatus.READ, LetterType.TO, session);
        }

        return letterDto;
    }

    @Override
    public List<LetterDto> getLetterListByUserId(LetterSearchDto letterSearchDto) {
        if (LetterType.TO.equals(letterSearchDto.getLetterType())) {
            return letterMapper.selectLetterListByUserIdAndTypeTo(letterSearchDto);
        }

        if (LetterType.FROM.equals(letterSearchDto.getLetterType())) {
            return letterMapper.selectLetterListByUserIdAndTypeFrom(letterSearchDto);
        }

        throw new IllegalArgumentException(ExceptionMessage.BAD_REQUEST);
    }

    @Override
    public boolean insertLetter(LetterInsertRequestDto requestDto, HttpSession session) {
        String fromUserId = SessionUtils.getSessionUserId(session);
        if (requestDto.getToUserId().equals(fromUserId)) {
            throw new IllegalArgumentException(ExceptionMessage.BAD_REQUEST);
        }

        // 실제 수신자가 존재하지 않는지 여부 확인
        if (ObjectUtils.isEmpty(userInfoService.selectUserInfoByUserId(requestDto.getToUserId()))) {
            throw new IllegalArgumentException(ExceptionMessage.NOT_FOUND);
        }

        LetterDto insertLetter =
                LetterDto.builder()
                        .letterTitle(requestDto.getLetterTitle())
                        .letterContent(requestDto.getLetterContent())
                        .toUserId(requestDto.getToUserId())
                        .toStatus(LetterStatus.UNREAD)
                        .fromUserId(fromUserId)
                        .fromStatus(LetterStatus.READ)
                        .createdAt(LocalDateTime.now())
                        .build();

        return letterMapper.insertLetter(insertLetter) > 0;
    }

    @Override
    public boolean deleteLetter(LetterDto letterDto, HttpSession session) {
        return updateLetterStatus(
                letterDto.getLetterId(), LetterStatus.DELETE, letterDto.getType(), session);
    }

    /**
     * ToStatus 또는 FromStatus 값을 Update한다
     *
     * @param letterId
     * @param status
     * @param letterType
     * @param session
     * @return
     */
    private boolean updateLetterStatus(
            int letterId, LetterStatus status, LetterType letterType, HttpSession session) {
        if (LetterType.TO.equals(letterType)) {
            return letterMapper.updateLetterToStatusByLetterIdAndToUserId(
                            LetterDto.builder()
                                    .letterId(letterId)
                                    .toStatus(status)
                                    .toUserId(SessionUtils.getSessionUserId(session))
                                    .build())
                    > 0;
        }

        if (LetterType.FROM.equals(letterType)) {
            return letterMapper.updateLetterFromStatusByLetterIdAndFromUserId(
                            LetterDto.builder()
                                    .letterId(letterId)
                                    .fromStatus(status)
                                    .fromUserId(SessionUtils.getSessionUserId(session))
                                    .build())
                    > 0;
        }

        // LetterConstants에 없는 Type(수신 또는 발신이 아닌경우) Exception
        throw new IllegalArgumentException(ExceptionMessage.BAD_REQUEST);
    }

    /**
     * 해당 쪽지가 발송자 또는 수신자인지 여부 확인
     *
     * @param letterDto
     * @param reqUserId
     * @return
     */
    private boolean isHaveGetLetterAuth(LetterDto letterDto, String reqUserId) {
        return isReqUserLetterTypeTo(letterDto, reqUserId)
                || isReqUserLetterTypeFrom(letterDto, reqUserId);
    }

    /**
     * 요청자가 쪽지의 수신자가 맞는지 확인
     *
     * @param letterDto
     * @param reqUserId
     * @return
     */
    private boolean isReqUserLetterTypeTo(LetterDto letterDto, String reqUserId) {
        return reqUserId.equals(letterDto.getToUserId());
    }

    /**
     * 요청자가 쪽지의 발송자가 맞는지 확인
     *
     * @param letterDto
     * @param reqUserId
     * @return
     */
    private boolean isReqUserLetterTypeFrom(LetterDto letterDto, String reqUserId) {
        return reqUserId.equals(letterDto.getFromUserId());
    }
}
