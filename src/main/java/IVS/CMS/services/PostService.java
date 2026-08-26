package IVS.CMS.services;

import IVS.CMS.services.dto.request.ReqPostCreateDTO;
import IVS.CMS.services.dto.request.ReqPostFilterDTO;
import IVS.CMS.services.dto.request.ReqPostUpdateDTO;
import IVS.CMS.services.dto.response.ResPostDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;

public interface PostService {
    ResPostDTO createPost(ReqPostCreateDTO req);

    ResPostDTO updatePost(long id, ReqPostUpdateDTO req);

    ResPostDTO getPostById(long id);

    ResultPaginationDTO getAllPosts(ReqPostFilterDTO filter, int page, int pageSize);

    void deletePost(long id);

    void changeStatus(long id, String status);
}