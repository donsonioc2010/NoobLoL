package com.nooblol.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.when;

import com.nooblol.global.dto.ResponseDto;
import com.nooblol.global.exception.ExceptionMessage;
import com.nooblol.user.dto.UserDto;
import com.nooblol.user.dto.UserSignUpRequestDto;
import com.nooblol.user.mapper.AdminMapper;
import com.nooblol.user.mapper.UserSignUpMapper;
import com.nooblol.user.utils.UserRoleStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @InjectMocks private AdminServiceImpl adminService;

    @Mock private UserSignUpMapper userSignUpMapper;

    @Mock private AdminMapper adminMapper;

    @Test
    @DisplayName("관리자를 계정을 추가할 때, DB에 삽입된 데이터가 없는경우, OK상태값과 false결과를 획득한다.")
    void addAdminMember_ResponseOkAndFalse() {
        // given
        UserSignUpRequestDto mockUserDto =
                new UserSignUpRequestDto()
                        .builder()
                        .userEmail("test@test.com")
                        .userName("테스트 관리자1")
                        .password("sample1")
                        .build();

        // mock
        when(userSignUpMapper.insertSignUpUser(mockUserDto)).thenReturn(0);

        // TODO : 추후 로그인 정보를 담는 부분 추가
        // when
        ResponseDto result = adminService.addAdminMember(mockUserDto);

        // then
        assertEquals(result.getResultCode(), HttpStatus.OK.value());
        assertEquals(result.getResult(), false);
    }

    @Test
    @DisplayName("관리자를 계정을 추가할 때, DB에 삽입된 데이터가 없는경우, OK상태값과 true 결과를 획득한다.")
    void addAdminMember_ResponseOkAndTrue() {
        // given
        UserSignUpRequestDto mockUserDto =
                new UserSignUpRequestDto()
                        .builder()
                        .userEmail("test@test.com")
                        .userName("테스트 관리자1")
                        .password("sample1")
                        .build();

        // mock
        when(userSignUpMapper.insertSignUpUser(mockUserDto)).thenReturn(1);

        // TODO : 추후 로그인 정보를 담는 부분 추가
        // when
        ResponseDto result = adminService.addAdminMember(mockUserDto);

        // then
        assertEquals(result.getResultCode(), HttpStatus.OK.value());
        assertEquals(result.getResult(), true);
    }

    @Test
    @DisplayName(
            "관리자를 계정을 추가할 때, SQLException이 아닌 다른 Exception이 발생하는 경우, BAD REQUEST메세지를 포함한 Exception을 획득한다.")
    void addAdminMember_Exception() {
        // given
        UserSignUpRequestDto mockUserDto =
                new UserSignUpRequestDto()
                        .builder()
                        .userEmail("test@test.com")
                        .userName("테스트 관리자1")
                        .password("sample1")
                        .build();

        // mock
        when(userSignUpMapper.insertSignUpUser(mockUserDto)).thenThrow(new RuntimeException());

        // when
        Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            adminService.addAdminMember(mockUserDto);
                        });

        // then
        assertEquals(e.getMessage(), ExceptionMessage.BAD_REQUEST);
    }

    // TODO : 로그인기능에서 Session Test Case 추가
    @Test
    @DisplayName("사용자의 강제 삭제를 할 경우, 삭제된 데이터가 있는 경우 Return으로 True를 획득한다.")
    void forceDeleteUser_WhenAdminThenReturnOkAndTrue() {
        // given
        String deleteUser = "testuser";

        // mock
        when(adminMapper.forcedDeleteUser(deleteUser)).thenReturn(1);

        // when
        ResponseDto result = adminService.forceDeleteUser(deleteUser);

        // then
        assertEquals(result.getResultCode(), HttpStatus.OK.value());
        assertTrue((boolean) result.getResult());
    }

    @Test
    @DisplayName("pageNum과 limitNum을 제공할 경우 사용자의 정보를 획득한다")
    void getAllUserList_WhenAdminThenResponseOk() {
        // given
        UserDto listUser1 =
                new UserDto()
                        .builder()
                        .userId("user1")
                        .userEmail("test1@test.com")
                        .userName("test1")
                        .userRole(UserRoleStatus.AUTH_USER.getRoleValue())
                        .build();

        UserDto listUser2 =
                new UserDto()
                        .builder()
                        .userId("user2")
                        .userEmail("test2@test.com")
                        .userName("test2")
                        .userRole(UserRoleStatus.AUTH_USER.getRoleValue())
                        .build();

        List<UserDto> mockReturnList = new ArrayList<>();
        mockReturnList.add(listUser1);
        mockReturnList.add(listUser2);

        // mock
        when(adminMapper.getAllUserList(0, 30)).thenReturn(mockReturnList);

        // when
        ResponseDto result = adminService.getAllUserList(0, 30);

        // then
        assertEquals(result.getResultCode(), HttpStatus.OK.value());
        assertEquals(mockReturnList, result.getResult());
    }

    @Test
    @DisplayName("사용자의 권한을 AUTH_USER로 변경하려는 경우, Response로 Ok와 결과값으로 true를 획득한다.")
    void changeToActiveUser_WhenAdminThenResponseOk() {
        // given
        String changeUserId = "test";

        // mock
        when(adminMapper.changeUserRole(any())).thenReturn(1);

        // when
        ResponseDto result = adminService.changeToActiveUser(changeUserId);

        // then
        assertEquals(result.getResultCode(), HttpStatus.OK.value());
        assertEquals(result.getResult(), true);
    }
}
