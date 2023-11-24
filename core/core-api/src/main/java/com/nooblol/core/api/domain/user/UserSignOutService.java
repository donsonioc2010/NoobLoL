package com.nooblol.user.service;

import com.nooblol.global.dto.ResponseDto;
import com.nooblol.user.dto.UserSignOutDto;

public interface UserSignOutService {

    ResponseDto signOutUser(UserSignOutDto userSignOutDto);
}
