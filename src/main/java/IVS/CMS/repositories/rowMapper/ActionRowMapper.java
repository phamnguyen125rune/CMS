package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Action;

@Component
public class ActionRowMapper implements RowMapper<Action> {
        @Override
        public Action mapRow(ResultSet rs, int rowNum) throws SQLException {

                Action action = new Action();

                action.setActionId(
                                rs.getLong("action_id"));

                action.setActionName(
                                rs.getString("action_name"));

                return action;
        }

        public MapSqlParameterSource toParams(Action action) {

                return new MapSqlParameterSource()
                                .addValue("actionId", action.getActionId())
                                .addValue("actionName", action.getActionName());
        }
}
