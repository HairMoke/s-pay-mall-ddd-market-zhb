package com.hb.api;

import com.hb.api.dto.CreatePayRequestDTO;
import com.hb.api.response.Response;

public interface IPayService {

    Response<String> createPayOrder(CreatePayRequestDTO createPayRequestDTO);
}
