/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.serviceproxy;

import github.benslabbert.vertxdaggercodegen.annotation.serviceproxy.GenerateProxies;
import github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto.MoreRequestDto;
import github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto.MoreResponseDto;
import github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto.RequestDto;
import github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto.ResponseDto;
import io.vertx.core.Future;

@GenerateProxies
public interface EventBusService {

  Future<ResponseDto> getValues(RequestDto request);

  Future<MoreResponseDto> getMoreValues(MoreRequestDto request);
}
