package core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    public void update(String sql, PreparedStatementSetter pss) throws DataAccessException{
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pss.setParameters(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(String sql, Object... parameters) throws DataAccessException{
        update(sql, createPrepardStatementSetter(parameters));
    }

    private PreparedStatementSetter createPrepardStatementSetter(Object... parameters) throws DataAccessException{
        return new PreparedStatementSetter() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                for (int i = 0; i < parameters.length; i++) {
                    pstmt.setObject(i + 1, parameters[i]);
                }
            }
        };
    }

    public <T>T queryForObject(String sql, RowMapper<T> rm, PreparedStatementSetter pss) throws DataAccessException{
        List<T> list = query(sql, rm, pss);
        if(list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T>T queryForObject(String sql, RowMapper<T> rm, Object... parameters) throws DataAccessException{
        return queryForObject(sql,rm, createPrepardStatementSetter(parameters));

    }

    public <T> List<T> query(String sql, RowMapper<T> rm, PreparedStatementSetter pss) throws DataAccessException{
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pss.setParameters(pstmt);

            ResultSet rs = pstmt.executeQuery();

            List<T> list = new ArrayList<T>();
            while (rs.next()) {
                T row = rm.mapRow(rs);
                list.add(row);
            }

            return list;
        }catch (SQLException e){
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, Object... parameters) throws DataAccessException {
        return query(sql,rm, createPrepardStatementSetter(parameters));

    }
}
