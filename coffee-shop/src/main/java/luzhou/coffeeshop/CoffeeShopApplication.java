package luzhou.coffeeshop;

import lombok.extern.slf4j.Slf4j;
import luzhou.coffeeshop.model.Coffee;
import luzhou.coffeeshop.model.CoffeeOrder;
import luzhou.coffeeshop.model.OrderState;
import luzhou.coffeeshop.repository.CoffeeOrderRepository;
import luzhou.coffeeshop.repository.CoffeeRepository;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@Slf4j
public class CoffeeShopApplication implements ApplicationRunner {
	@Autowired
	private CoffeeRepository coffeeRepository;

	@Autowired
	private CoffeeOrderRepository coffeeOrderRepository;

	public static void main(String[] args) {
		SpringApplication.run(CoffeeShopApplication.class, args);
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		initOrders();
		findOrders();
	}

	private void findOrders() {
		coffeeRepository
				.findAll(Sort.by(Sort.Direction.ASC, "name"))
				.forEach(c -> log.info("Loading {}", c));

		List<CoffeeOrder> list = coffeeOrderRepository.findTop3ByOrderByUpdateTimeDescIdAsc();
		log.info("find Top 3 by Order by Update Time Desc Id Asc: {}", getJoinedOrderId(list));

		list = coffeeOrderRepository.findByCustomerOrderById("Zhang San");
		log.info("find by customer order by id: {}", getJoinedOrderId(list));

		list.forEach(o -> {
			log.info("Order {}", o.getId());
			o.getItems().forEach(i -> log.info(" Item {}", i));
		});

		list = coffeeOrderRepository.findByItems_Name("latte");
		log.info("find by items_name: {}", getJoinedOrderId(list));
	}

	private void initOrders() {
		Coffee latte = Coffee.builder().name("latte")
				.price(Money.of(CurrencyUnit.of("CNY"), 30.0))
				.build();
		coffeeRepository.save(latte);
		log.info("Coffee: {}", latte);

		Coffee espresso = Coffee.builder().name("espresso")
						.price(Money.of(CurrencyUnit.of("CNY"), 20.0))
						.build();
		coffeeRepository.save(espresso);
		log.info("Coffee: {}", espresso);

		CoffeeOrder order = CoffeeOrder.builder()
						.customer("Zhang San")
						.items(Collections.singletonList(espresso))
						.state(OrderState.INIT)
						.build();
		coffeeOrderRepository.save(order);
		log.info("Order: {}", order);

		order = CoffeeOrder.builder()
						.customer("Zhang San")
						.items(Arrays.asList(espresso, latte))
						.state(OrderState.INIT)
						.build();
		coffeeOrderRepository.save(order);
		log.info("Order: {}", order);
	}

	private String getJoinedOrderId(List<CoffeeOrder> list) {
		return list.stream().map(o -> o.getId().toString())
				.collect(Collectors.joining(","));
	}
}
