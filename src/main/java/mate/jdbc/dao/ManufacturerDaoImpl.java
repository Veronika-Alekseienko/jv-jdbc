package mate.jdbc.dao;

import mate.jdbc.lib.Dao;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        String insertManufacturerRequest = "INSERT INTO manufacturers(name,country) values(?,?);";
        try (Connection connection = ConnectionUtil.getConnection();
              PreparedStatement createManufacturerStatement =
                      connection.prepareStatement(insertManufacturerRequest, Statement.RETURN_GENERATED_KEYS)) {
            createManufacturerStatement.setString(1, manufacturer.getName());
            createManufacturerStatement.setString(2, manufacturer.getCountry());
            createManufacturerStatement.executeUpdate();
            ResultSet generatedKeys = createManufacturerStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                manufacturer.setId(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert manufacturer to DB" + manufacturer, e);
        }
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        String selectManufacturerRequest = "SELECT * FROM manufacturers WHERE id = ? AND is_deleted = false";
        try ( Connection connection = ConnectionUtil.getConnection();
              PreparedStatement getAManufacturerStatement =
                      connection.prepareStatement(selectManufacturerRequest)) {
            getAManufacturerStatement.setLong(1, id);
            ResultSet resultSet = getAManufacturerStatement.executeQuery();
            Manufacturer manufacturer = null;
            if (resultSet.next()) {
                manufacturer = parseResultSet(resultSet);
            }
            return Optional.ofNullable(manufacturer);
        } catch (SQLException e) {
            throw new RuntimeException("Can't get all manufacturers from DB", e);
        }
    }

    @Override
    public List<Manufacturer> getAll() {
        List<Manufacturer> allManufacturers = new ArrayList<>();
        try ( Connection connection = ConnectionUtil.getConnection();
              Statement getAllManufacturerStatement = connection.createStatement()) {
            ResultSet resultSet = getAllManufacturerStatement.executeQuery("SELECT * FROM manufacturers");
            while (resultSet.next()) {
                allManufacturers.add(parseResultSet(resultSet));
            }
            return allManufacturers;
        } catch (SQLException e) {
            throw new RuntimeException("Can't get all manufacturers from DB", e);
        }
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        String updateManufacturerRequest = "UPDATE manufacturers SET name = ? where id = ? AND is_deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
        PreparedStatement updateManufacturerStatement = connection.prepareStatement(updateManufacturerRequest)) {
            updateManufacturerStatement.setString(1, manufacturer.getName());
            updateManufacturerStatement.setObject(2, manufacturer.getId());
            updateManufacturerStatement.executeUpdate();
            return manufacturer;
        } catch (SQLException e) {
            throw new RuntimeException("Can't update manufacturer from DB" + manufacturer, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteManufacturerRequest = "UPDATE manufacturers SET is_deleted = true WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement createManufacturerStatement =
                     connection.prepareStatement(deleteManufacturerRequest)) {
            createManufacturerStatement.setLong(1, id);
           return createManufacturerStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert manufacturer to DB" + id, e);
        }
    }
    private Manufacturer parseResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String country = resultSet.getString("country");
        return new Manufacturer(id, name, country);
    }
}