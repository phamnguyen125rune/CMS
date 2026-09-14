package IVS.CMS.services.impl;

import IVS.CMS.domain.FormDetail;
import IVS.CMS.services.dto.request.ReqCreateFormDetailDTO;
import IVS.CMS.services.dto.request.ReqReplyFormDetailDTO; // Tái sử dụng hoặc bạn đổi tên thành ReqReplyFormDTO
import IVS.CMS.services.dto.response.PaginationResponseDTO;
import IVS.CMS.repositories.FormDetailRepository;
import IVS.CMS.services.FormDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormDetailServiceImpl implements FormDetailService {

    private final FormDetailRepository formDetailRepository;

    public FormDetailServiceImpl(FormDetailRepository formDetailRepository) {
        this.formDetailRepository = formDetailRepository;
    }

    @Override
    public FormDetail createFormDetail(ReqCreateFormDetailDTO dto) {
        FormDetail formDetail = new FormDetail();
        formDetail.setFullName(dto.getFullName());
        formDetail.setEmail(dto.getEmail());
        formDetail.setPhoneNumber(dto.getPhoneNumber());
        formDetail.setCompany(dto.getCompany());
        formDetail.setFormCategoryId(dto.getFormCategoryId()); // Lấy ID category từ request
        formDetail.setMessage(dto.getMessage());
        formDetail.setStatus("new");

        return formDetailRepository.save(formDetail);
    }

    @Override
    public FormDetail getFormDetailById(Long id) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));

        // Tự động chuyển status khi nhân viên click xem chi tiết form
        if ("new".equalsIgnoreCase(formDetail.getStatus())) {
            formDetailRepository.updateStatus(id, "read");
            formDetail.setStatus("read");
        }

        return formDetail;
    }

    @Override
    public PaginationResponseDTO getAllFormDetails(String search, String status, int page, int size) {
        try {
            List<FormDetail> list = formDetailRepository.findAll(search, status, page, size);
            long totalElements = formDetailRepository.count(search, status);
            int totalPages = (int) Math.ceil((double) totalElements / size);

            PaginationResponseDTO.Meta meta = new PaginationResponseDTO.Meta();
            meta.setPage(page);
            meta.setPageSize(size);
            meta.setPages(totalPages);
            meta.setTotal(totalElements);

            PaginationResponseDTO response = new PaginationResponseDTO();
            response.setMeta(meta);
            response.setResult(list);

            return response;
        } catch (Exception e) {
            System.err.println("[FormDetailServiceImpl.getAllFormDetails] ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public FormDetail replyFormDetail(Long id, ReqReplyFormDetailDTO dto) {
        FormDetail formDetail = getFormDetailById(id);
        formDetailRepository.updateReply(id, dto.getReplyMessage(), "replied");
        formDetail.setReplyMessage(dto.getReplyMessage());
        formDetail.setStatus("replied");
        return formDetail;
    }

    @Override
    public FormDetail updateFormDetailStatus(Long id, String status) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));

        formDetailRepository.updateStatus(id, status);
        formDetail.setStatus(status);

        return formDetail;
    }

    @Override
    public void deleteFormDetail(Long id) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));
        formDetailRepository.deleteById(formDetail.getFormId());
    }
}