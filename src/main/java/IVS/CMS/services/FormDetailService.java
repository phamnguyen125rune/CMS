package IVS.CMS.services;

import IVS.CMS.domain.FormDetail;
import IVS.CMS.services.dto.request.ReqCreateFormDetailDTO;
import IVS.CMS.services.dto.request.ReqReplyFormDetailDTO; // Tái sử dụng hoặc bạn đổi tên thành ReqReplyFormDTO
import IVS.CMS.services.dto.response.PaginationResponseDTO;

public interface FormDetailService {
    FormDetail createFormDetail(ReqCreateFormDetailDTO dto);
    FormDetail getFormDetailById(Long id);
    PaginationResponseDTO getAllFormDetails(String search, String status, int page, int size);
    FormDetail replyFormDetail(Long id, ReqReplyFormDetailDTO dto);
    void deleteFormDetail(Long id);
    FormDetail updateFormDetailStatus(Long id, String status);
}