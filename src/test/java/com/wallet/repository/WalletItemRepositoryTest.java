package com.wallet.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.util.enums.TypeEnum;

@RunWith(SpringRunner.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class WalletItemRepositoryTest {

	private static final Date DATE = new Date();
	private static final TypeEnum TYPE = TypeEnum.EN;
	private static final String DESCRIPTION = "Conta de Luz";
	private static final BigDecimal VALUE = BigDecimal.valueOf(65);
	private Long savedWalletItemId = null;
	private Long savedWalletId = null;
	
	@Autowired
	WalletItemRepository repository;
	
	@Autowired
	WalletRepository walletRepository;
	
	@BeforeEach
	public void setUp() {
		Wallet w = new Wallet();
		w.setName("Carteira Teste");
		w.setValue(BigDecimal.valueOf(250));
		walletRepository.save(w);
		
		WalletItem wi = new WalletItem(null, w, DATE, TYPE, DESCRIPTION, VALUE);
		repository.save(wi);
		
		savedWalletItemId = wi.getId();
		savedWalletId = w.getId();
	}
	
	@AfterEach
	public void tearDown() {
		repository.deleteAll();
		walletRepository.deleteAll();
	}
	
	@Test
	@Order(1)
	public void testSave() {
		
		Wallet w = new Wallet();
		w.setName("Carteira 1");
		w.setValue(BigDecimal.valueOf(500));
		walletRepository.save(w);
		
		WalletItem wi = new WalletItem(1L, w, DATE, TYPE, DESCRIPTION, VALUE);
		
		WalletItem response = repository.save(wi);
		
		assertNotNull(response);
		assertEquals(DESCRIPTION, response.getDescription());
		assertEquals(TYPE, response.getType());
		assertEquals(VALUE, response.getValue());
		assertEquals(w.getId(), response.getWallet().getId());
	}
	
	@Test
	@Order(2)
	public void testSaveInvlaidWalletItem() {
		Assertions.assertThrows(ConstraintViolationException.class, new Executable() {
			
			@Override
			public void execute() throws Throwable {
				WalletItem wi = new WalletItem(null, null, DATE, null, DESCRIPTION, null);
				repository.save(wi);
			}
		});
	}
	
	@Test
	@Order(3)
	public void testUpdate() {
		Optional<WalletItem> wi = repository.findById(savedWalletItemId);
		
		String description = "Descrição alterada";
		
		WalletItem changed = wi.get();
		changed.setDescription(description);
		
		repository.save(changed);
		
		Optional<WalletItem> newWalletItem = repository.findById(savedWalletItemId);
		
		assertEquals(description, newWalletItem.get().getDescription());
	}
	
	@Test
	@Order(4)
	public void deleteWalletItem() {
		Optional<Wallet> wallet = walletRepository.findById(savedWalletId);
		WalletItem wi = new WalletItem(null, wallet.get(), DATE, TYPE, DESCRIPTION, VALUE);
		
		repository.save(wi);
		
		repository.deleteById(wi.getId());
		
		Optional<WalletItem> response = repository.findById(wi.getId());
		
		assertFalse(response.isPresent());
	}
	
	@Test
	@Order(5)
	public void testFindBetweenDates() {
		Optional<Wallet> w = walletRepository.findById(savedWalletId);
		
		LocalDateTime localDateTime = DATE.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		Date currentDatePlusFiveDays = Date.from(localDateTime.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());
		Date currentDatePlusSevenDays = Date.from(localDateTime.plusDays(7).atZone(ZoneId.systemDefault()).toInstant());
		
		repository.save(new WalletItem(null, w.get(), currentDatePlusFiveDays, TYPE, DESCRIPTION, VALUE));
		repository.save(new WalletItem(null, w.get(), currentDatePlusSevenDays, TYPE, DESCRIPTION, VALUE));
		
		PageRequest pg = PageRequest.of(0, 10);
		Page<WalletItem> response = repository.findAllByWalletIdAndDateGreaterThanEqualAndDateLessThanEqual(savedWalletId, DATE, currentDatePlusFiveDays, pg);
		
		assertEquals(2, response.getContent().size());
		assertEquals(2, response.getTotalElements());
		assertEquals(savedWalletId, response.getContent().get(0).getWallet().getId());
	}
	
	@Test
	@Order(6)
	public void testFindByType() {
		List<WalletItem> response  = repository.findByWalletIdAndType(savedWalletId, TYPE);
		
		assertEquals(1, response.size());
		assertEquals(TYPE, response.get(0).getType());
	}
	
	@Test
	@Order(7)
	public void testFindByTypeSd() {
		Optional<Wallet> w = walletRepository.findById(savedWalletId);
		
		repository.save(new WalletItem(null, w.get(), DATE, TypeEnum.SD, DESCRIPTION, VALUE));
		
		List<WalletItem> response = repository.findByWalletIdAndType(savedWalletId, TypeEnum.SD);
		
		assertEquals(1, response.size());
		assertEquals(TypeEnum.SD, response.get(0).getType());
	}
	
	@Test
	@Order(8)
	public void testSumByWallet() {
		Optional<Wallet> w = walletRepository.findById(savedWalletId);
		
		repository.save(new WalletItem(null, w.get(), DATE, TYPE, DESCRIPTION, BigDecimal.valueOf(150.80)));
		
		BigDecimal response = repository.sumByWalletId(savedWalletId);
		
		assertEquals(0, response.compareTo(BigDecimal.valueOf(215.8)));
	}
}
