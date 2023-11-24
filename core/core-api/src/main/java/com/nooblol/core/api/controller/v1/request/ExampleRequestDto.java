package com.nooblol.core.api.controller.v1.request;

import com.nooblol.core.api.domain.ExampleData;

public record ExampleRequestDto(String data) {
    public ExampleData toExampleData() {
        return new ExampleData(data, data);
    }
}
