package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Api;

@Component
public class ApiRowMapper implements RowMapper<Api>{
        @Override
        public Api mapRow(ResultSet rs, int rowNum) throws SQLException {

                Api api = new Api();

                api.setApiId(
                        rs.getLong("api_id")
                );

                api.setApiLink(
                        rs.getString("api_link")
                );

                api.setApiDescription(
                        rs.getString("api_description")
                );
                return api;
        }

    public MapSqlParameterSource toParams(Api api) {

        return new MapSqlParameterSource()
                .addValue("apiId", api.getApiId())
                .addValue("apiLink", api.getApiLink())
                .addValue("apiDescription", api.getApiDescription());
    }
}
