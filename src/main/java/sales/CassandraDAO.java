package sales;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CassandraDAO implements DatabaseInterface {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraDAO.class);
    private final CqlSession session;
    private static final String KEYSPACE = "real_estate";
    private static final String TABLE_NAME = "home_sales";
    private static final String VIEWS_TABLE = "view_stats";
    private static final int DEFAULT_LIMIT = 100;

    public CassandraDAO() {
        // Connect to Cassandra
        this.session = CqlSession.builder()
                .withKeyspace(KEYSPACE)
                .build();

        // Create keyspace and table if they don't exist
        createKeyspaceAndTable();
    }

    private void createKeyspaceAndTable() {
        // Create keyspace if it doesn't exist
        session.execute(
            SchemaBuilder.createKeyspace(KEYSPACE)
                .ifNotExists()
                .withSimpleStrategy(1)
                .build()
        );

        // Drop view_stats table if it exists to ensure proper counter column setup
        try {
            session.execute(
                SchemaBuilder.dropTable(KEYSPACE, VIEWS_TABLE)
                    .ifExists()
                    .build()
            );
            LOG.info("Dropped existing view_stats table");
        } catch (Exception e) {
            LOG.warn("Error dropping view_stats table: {}", e.getMessage());
        }

        // Create home_sales table if it doesn't exist
        session.execute(
            SchemaBuilder.createTable(KEYSPACE, TABLE_NAME)
                .ifNotExists()
                .withPartitionKey("property_id", DataTypes.TEXT)
                .withColumn("download_date", DataTypes.TEXT)
                .withColumn("council_name", DataTypes.TEXT)
                .withColumn("purchase_price", DataTypes.TEXT)
                .withColumn("address", DataTypes.TEXT)
                .withColumn("post_code", DataTypes.TEXT)
                .withColumn("property_type", DataTypes.TEXT)
                .withColumn("strata_lot_number", DataTypes.TEXT)
                .withColumn("property_name", DataTypes.TEXT)
                .withColumn("area", DataTypes.TEXT)
                .withColumn("area_type", DataTypes.TEXT)
                .withColumn("contract_date", DataTypes.TEXT)
                .withColumn("settlement_date", DataTypes.TEXT)
                .withColumn("zoning", DataTypes.TEXT)
                .withColumn("nature_of_property", DataTypes.TEXT)
                .withColumn("primary_purpose", DataTypes.TEXT)
                .withColumn("legal_description", DataTypes.TEXT)
                .build()
        );

        // Create view_stats table with counter column
        session.execute(
            SchemaBuilder.createTable(KEYSPACE, VIEWS_TABLE)
                .ifNotExists()
                .withPartitionKey("id", DataTypes.TEXT)
                .withColumn("view_count", DataTypes.COUNTER)
                .build()
        );
        LOG.info("Created view_stats table with counter column");

        // Create secondary indexes for querying
        session.execute(
            SchemaBuilder.createIndex("idx_post_code")
                .ifNotExists()
                .onTable(KEYSPACE, TABLE_NAME)
                .andColumn("post_code")
                .build()
        );

        session.execute(
            SchemaBuilder.createIndex("idx_area_type")
                .ifNotExists()
                .onTable(KEYSPACE, TABLE_NAME)
                .andColumn("area_type")
                .build()
        );

        session.execute(
            SchemaBuilder.createIndex("idx_purchase_price")
                .ifNotExists()
                .onTable(KEYSPACE, TABLE_NAME)
                .andColumn("purchase_price")
                .build()
        );
    }

    @Override
    public boolean newSale(HomeSale homeSale) {
        try {
            Insert insert = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
                .value("property_id", QueryBuilder.literal(homeSale.getSaleID()))
                .value("post_code", QueryBuilder.literal(homeSale.getPostcode()))
                .value("purchase_price", QueryBuilder.literal(String.valueOf(homeSale.getSalePrice())))
                .value("area_type", QueryBuilder.literal(homeSale.getarea_type()));

            session.execute(insert.build());
            return true;
        } catch (Exception e) {
            System.err.println("Error creating new sale: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<HomeSale> getSaleById(String saleId) {
        try {
            Select select = QueryBuilder.selectFrom(KEYSPACE, TABLE_NAME)
                .all()
                .whereColumn("property_id").isEqualTo(QueryBuilder.literal(saleId));

            ResultSet rs = session.execute(select.build());
            Row row = rs.one();

            if (row != null) {
                return Optional.of(new HomeSale(
                    row.getString("property_id"),
                    row.getString("post_code"),
                    Integer.parseInt(row.getString("purchase_price")),
                    row.getString("area_type")
                ));
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error getting sale by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<HomeSale> getSalesByPostCode(String postcode) {
        try {
            Select select = QueryBuilder.selectFrom(KEYSPACE, TABLE_NAME)
                .all()
                .whereColumn("post_code").isEqualTo(QueryBuilder.literal(postcode))
                .limit(DEFAULT_LIMIT);

            ResultSet rs = session.execute(select.build());
            List<HomeSale> sales = new ArrayList<>();

            for (Row row : rs) {
                try {
                    sales.add(new HomeSale(
                        row.getString("property_id"),
                        row.getString("post_code"),
                        Integer.parseInt(row.getString("purchase_price")),
                        row.getString("area_type")
                    ));
                } catch (NumberFormatException e) {
                    LOG.warn("Skipping record with invalid price format: {}", row.getString("purchase_price"));
                }
            }
            return sales;
        } catch (Exception e) {
            LOG.error("Error getting sales by postcode: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HomeSale> getSalesByAreaType(String areaType) {
        try {
            Select select = QueryBuilder.selectFrom(KEYSPACE, TABLE_NAME)
                .all()
                .whereColumn("area_type").isEqualTo(QueryBuilder.literal(areaType))
                .limit(DEFAULT_LIMIT);

            ResultSet rs = session.execute(select.build());
            List<HomeSale> sales = new ArrayList<>();

            for (Row row : rs) {
                try {
                    sales.add(new HomeSale(
                        row.getString("property_id"),
                        row.getString("post_code"),
                        Integer.parseInt(row.getString("purchase_price")),
                        row.getString("area_type")
                    ));
                } catch (NumberFormatException e) {
                    LOG.warn("Skipping record with invalid price format: {}", row.getString("purchase_price"));
                }
            }
            return sales;
        } catch (Exception e) {
            LOG.error("Error getting sales by area type: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HomeSale> getSalesByPriceRange(String min, String max) {
        try {
            int minPrice = Integer.parseInt(min);
            int maxPrice = Integer.parseInt(max);

            // First get all sales and filter in memory since Cassandra doesn't support range queries on text fields
            Select select = QueryBuilder.selectFrom(KEYSPACE, TABLE_NAME)
                .all()
                .limit(DEFAULT_LIMIT * 10); // Get more records to filter

            ResultSet rs = session.execute(select.build());
            List<HomeSale> sales = new ArrayList<>();

            for (Row row : rs) {
                try {
                    String priceStr = row.getString("purchase_price");
                    int price = Integer.parseInt(priceStr);
                    if (price >= minPrice && price <= maxPrice) {
                        sales.add(new HomeSale(
                            row.getString("property_id"),
                            row.getString("post_code"),
                            price,
                            row.getString("area_type")
                        ));
                    }
                } catch (NumberFormatException e) {
                    LOG.warn("Skipping record with invalid price format: {}", row.getString("purchase_price"));
                }
            }

            // Limit the results after filtering
            if (sales.size() > DEFAULT_LIMIT) {
                sales = sales.subList(0, DEFAULT_LIMIT);
            }

            LOG.info("Found {} sales in price range {} to {}", sales.size(), minPrice, maxPrice);
            return sales;
        } catch (Exception e) {
            LOG.error("Error getting sales by price range: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HomeSale> getAllSales() {
        try {
            Select select = QueryBuilder.selectFrom(KEYSPACE, TABLE_NAME)
                .all()
                .limit(DEFAULT_LIMIT);

            ResultSet rs = session.execute(select.build());
            List<HomeSale> sales = new ArrayList<>();

            for (Row row : rs) {
                try {
                    sales.add(new HomeSale(
                        row.getString("property_id"),
                        row.getString("post_code"),
                        Integer.parseInt(row.getString("purchase_price")),
                        row.getString("area_type")
                    ));
                } catch (NumberFormatException e) {
                    LOG.warn("Skipping record with invalid price format: {}", row.getString("purchase_price"));
                }
            }

            if (sales.isEmpty()) {
                LOG.info("No sales found in the database");
            } else {
                LOG.info("Retrieved {} sales from the database", sales.size());
            }

            return sales;
        } catch (Exception e) {
            LOG.error("Error getting all sales: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
        }
    }

    @Override
    public void incrementViews(String id) {
        try {
            // Increment the counter directly - no need to create record first
            session.execute(
                QueryBuilder.update(KEYSPACE, VIEWS_TABLE)
                    .increment("view_count", QueryBuilder.literal(1L))
                    .whereColumn("id").isEqualTo(QueryBuilder.literal(id))
                    .build()
            );
            LOG.info("Incremented view count for {}", id);
        } catch (Exception e) {
            LOG.error("Error incrementing view count for {}: {}", id, e.getMessage(), e);
        }
    }

    @Override
    public ViewStats getViewStats(String id) {
        try {
            // Get current view count
            Select select = QueryBuilder.selectFrom(KEYSPACE, VIEWS_TABLE)
                .column("view_count")
                .whereColumn("id").isEqualTo(QueryBuilder.literal(id));

            ResultSet rs = session.execute(select.build());
            Row row = rs.one();

            if (row != null) {
                long viewCount = row.getLong("view_count");
                LOG.info("Retrieved view count {} for {}", viewCount, id);
                return new ViewStats(id, (int) viewCount);
            } else {
                LOG.info("No view stats found for {}, returning 0", id);
                return new ViewStats(id, 0);
            }
        } catch (Exception e) {
            LOG.error("Error getting view stats for {}: {}", id, e.getMessage(), e);
            return new ViewStats(id, 0);
        }
    }
}