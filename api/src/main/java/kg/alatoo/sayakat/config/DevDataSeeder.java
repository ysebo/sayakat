package kg.alatoo.sayakat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import kg.alatoo.sayakat.marketplace.Provider;
import kg.alatoo.sayakat.marketplace.ProviderRepository;
import kg.alatoo.sayakat.poi.DatasetImporter;
import kg.alatoo.sayakat.routing.TravelMatrixService;
import kg.alatoo.sayakat.user.AuthService;
import kg.alatoo.sayakat.user.Role;
import kg.alatoo.sayakat.user.User;
import kg.alatoo.sayakat.user.UserRepository;

@Component
@ConditionalOnProperty(name = "sayakat.dev.seed", havingValue = "true")
public class DevDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository users;
    private final ProviderRepository providers;
    private final AuthService authService;
    private final DatasetImporter importer;
    private final TravelMatrixService matrixService;
    private final JdbcTemplate jdbc;
    private final String demoPassword;

    public DevDataSeeder(UserRepository users, ProviderRepository providers, AuthService authService,
                         DatasetImporter importer, TravelMatrixService matrixService, JdbcTemplate jdbc,
                         @Value("${sayakat.dev.demo-password}") String demoPassword) {
        this.users = users;
        this.providers = providers;
        this.authService = authService;
        this.importer = importer;
        this.matrixService = matrixService;
        this.jdbc = jdbc;
        this.demoPassword = demoPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (users.count() == 0) {
            authService.createUser("admin@sayakat.local", demoPassword, "Demo Admin", Role.ADMIN);
            authService.createUser("tourist@sayakat.local", demoPassword, "Demo Tourist", Role.TOURIST);
            User providerUser = authService.createUser("provider@sayakat.local", demoPassword, "Demo Provider",
                    Role.PROVIDER);
            Provider provider = new Provider();
            provider.setUser(providerUser);
            provider.setDisplayName("Sayakat Demo Hosts");
            provider.setDescription("Seeded guesthouses and yurt camps for development");
            providers.save(provider);
            log.info("Seeded demo users (password from sayakat.dev.demo-password)");
        }
        if (count("poi") == 0) {
            log.info("Imported {} POIs", importer.importPois());
        }
        if (count("service_offering") == 0) {
            Provider demo = users.findByEmailIgnoreCase("provider@sayakat.local")
                    .flatMap(u -> providers.findByUserId(u.getId()))
                    .orElseThrow();
            log.info("Imported {} lodgings", importer.importLodgings(demo));
        }
        if (count("travel_leg") == 0) {
            matrixService.rebuild();
        }
    }

    private long count(String table) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return n == null ? 0 : n;
    }
}
