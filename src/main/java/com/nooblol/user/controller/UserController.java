package com.nooblol.user.controller;

import com.nooblol.global.annotation.UserLoginCheck;
import com.nooblol.global.dto.ResponseDto;
import com.nooblol.global.utils.RegexConstants;
import com.nooblol.user.dto.UserInfoUpdateDto;
import com.nooblol.user.dto.UserLoginDto;
import com.nooblol.user.dto.UserSignOutDto;
import com.nooblol.user.dto.UserSignUpRequestDto;
import com.nooblol.user.service.UserInfoService;
import com.nooblol.user.service.UserSignOutService;
import com.nooblol.user.service.UserSignUpService;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserSignUpService userSignUpService;

    private final UserSignOutService userSignOutService;

    private final UserInfoService userInfoService;

    /**
     * 사용자의 회원가입
     *
     * @param userSignUpDto
     * @return
     */
    @PostMapping("/signup")
    public ResponseDto signUpSubmit(@Valid @RequestBody UserSignUpRequestDto userSignUpDto) {
        return userSignUpService.signUpUser(userSignUpDto);
    }

    /**
     * 사용자의 정보 수정 기능.
     *
     * <p>UserName과 UserPassword에 대해서만 수정이 가능하며, 관리자와 메일을 인증한 사용자만 사용자 정보에 대하여 변경이 가능하다.
     *
     * @param userInfoUpdateDto
     * @return 정상적으로 Update가 성공했으면 OK Response와 결과값으로 true가 Return되며, 정보 수정에 실패하면 OK상태코드와 false값을
     *     Return한다
     */
    @PostMapping("/")
    public ResponseDto userUpdate(@Valid @RequestBody UserInfoUpdateDto userInfoUpdateDto) {
        return userInfoService.updateUserInfo(userInfoUpdateDto);
    }

    /**
     * 회원탈퇴로 DB에 Role의 상태값을 변경해서 데이터를 보존하는게 아닌 Delete를 시켜버린다.
     *
     * @param userSignOutDto UserId, Password를 Parameter로 받는다
     * @return
     */
    @DeleteMapping("/signout")
    public ResponseDto signOutSubmit(@Valid @RequestBody UserSignOutDto userSignOutDto) {
        return userSignOutService.signOutUser(userSignOutDto);
    }

    /**
     * 사용자 로그인을 진행한다
     *
     * @param userLoginDto
     * @param session
     * @return
     */
    @PostMapping("/login")
    public ResponseDto userLogin(@Valid @RequestBody UserLoginDto userLoginDto, HttpSession session) {
        return userInfoService.userLogin(userLoginDto, session);
    }

    /**
     * 사용자의 로그아웃 로그인이 되어있어야만 한다.
     *
     * @param session
     * @return
     */
    @UserLoginCheck
    @PostMapping("/logout")
    public ResponseDto userLogout(HttpSession session) {
        return userInfoService.userLogout(session);
    }

    /**
     * E-mail파라미터를 받아, 해당 메일주소를
     *
     * @param email
     * @return
     */
    @GetMapping("/resend-authmail/{email:.+}")
    public ResponseDto resendAuthMail(
            @PathVariable
                    @NotBlank
                    @Pattern(regexp = RegexConstants.MAIL_REGEX, message = "이메일 형식에 맞지 않습니다.")
                    String email) {
        return userSignUpService.reSendSignUpUserMail(email.trim());
    }

    /**
     * UserId를 받아 비활성화가 진행된 사용자의 UserRole을 활성상태인 `UserRoleStatus.AUTH_USER`로 변경함
     *
     * @param userId users테이블의 userId Value
     * @return 404 : userId의 정상적인 입력이 아님, 500 : DB처리 도중 문제 발생, OK true: 정상적인 변경, OK false : Update
     *     Fail
     */
    @GetMapping("/auth/{userId}")
    public ResponseDto authUserByMail(@PathVariable @NotBlank String userId) {
        return userSignUpService.changeRoleAuthUser(userId.trim());
    }
}
