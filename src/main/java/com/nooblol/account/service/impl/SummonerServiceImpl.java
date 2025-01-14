package com.nooblol.account.service.impl;

import static com.nooblol.global.utils.StringUtils.summonerNameWhiteSpaceReplace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nooblol.account.dto.summoner.SummonerDto;
import com.nooblol.account.mapper.SummonerMapper;
import com.nooblol.account.service.SummonerService;
import com.nooblol.global.config.RiotConfiguration;
import com.nooblol.global.dto.ResponseDto;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SummonerServiceImpl implements SummonerService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RiotConfiguration riotConfiguration;
    private final SummonerMapper summonerMapper;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final HttpHeaders initRiotHeader;

    @Override
    public ResponseDto getSummonerAccointInfo(String summonerName) {
        if (StringUtils.isBlank(summonerName)) {
            throw new IllegalArgumentException("소환사명이 입력되지 않았습니다");
        }
        return summonerAccountProcess(summonerName);
    }

    @Override
    public ResponseDto summonerAccountProcess(String summonerName) {
        ResponseDto responseDto = selectSummonerAccountByRiot(summonerName);
        summonerAccountDBProcess(responseDto);
        return responseDto;
    }

    @Override
    public void summonerAccountDBProcess(ResponseDto responseDto) {
        if (responseDto.getResultCode() == HttpStatus.OK.value()) {
            SummonerDto riotSearchData = (SummonerDto) responseDto.getResult();
            SummonerDto serviceDBData = selectSummonerAccountByDB(riotSearchData);

            if (ObjectUtils.isEmpty(serviceDBData)) {
                boolean isSame = riotSearchData.equals(serviceDBData);
                if (!isSame) {
                    summonerMapper.updateSummonerAccount(riotSearchData);
                }
            } else {
                summonerMapper.insertSummonerAccount(riotSearchData);
                return;
            }

            if (!riotSearchData.equals(serviceDBData)) {
                summonerMapper.updateSummonerAccount(riotSearchData);
            }
        }
    }

    @Override
    public ResponseDto selectSummonerAccountByRiot(String summonerName) {
        summonerName = summonerNameWhiteSpaceReplace(summonerName);
        String url =
                riotConfiguration.getSummonerDomain()
                        + riotConfiguration.getSummonerNameSearchByNameApi()
                        + summonerName;
        return responseResult(url, SummonerDto.class);
    }

    @Override
    public SummonerDto selectSummonerAccountByDB(SummonerDto summonerDto) {
        return summonerMapper.selectSummonerAccount(summonerDto.getId());
    }

    /**
     * Http통신을 통해 response를 받은 데이터를 ResponseDto로 가공한다. resultClass 맞게 데이터를 result로 삽입하며, 통신오류 코드를 받은
     * 경우에는 해당 코드를 기반으로 HttpStatus의 상태값을 받으며 HttpStatus에 없는 값이거나 또는 Exception이 발생한 경우에는 NOT_FOUND로
     * ResponseDto를 반환받는다.
     *
     * @param url
     * @param resultClass
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> ResponseDto responseResult(String url, Class<T> resultClass) {
        ResponseDto rtnData = null;
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url, HttpMethod.GET, new HttpEntity<String>(initRiotHeader), String.class);

            rtnData = makeResponseDto(response, resultClass);
        } catch (Exception e) {
            /* 최초 Exception을 생각할 시 ObjectMapper변환만 생각했으나
            Riot과 통신과정의 Exception을 생각하여 추가함 */
            log.error(e.getMessage());
        } finally {
            if (rtnData == null) {
                rtnData = new ResponseDto(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND);
            }
            return rtnData;
        }
    }

    private <T> T getResponseBody(ResponseEntity response, Class<T> dto) throws IOException {
        String body = response.getBody().toString();
        return objectMapper.readValue(body, dto);
    }

    private <T> ResponseDto makeResponseDto(ResponseEntity response, Class<T> resultClass)
            throws IOException {
        HttpStatus sameStatus = HttpStatus.valueOf(response.getStatusCode().value());
        if (sameStatus == HttpStatus.OK) {
            return new ResponseDto(sameStatus.value(), getResponseBody(response, resultClass));
        }
        if (sameStatus != null) {
            return new ResponseDto(sameStatus.value(), sameStatus);
        }
        return new ResponseDto(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND);
    }
}
