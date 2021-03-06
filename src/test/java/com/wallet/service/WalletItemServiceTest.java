package com.wallet.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.repository.WalletItemRepository;
import com.wallet.util.enums.TypeEnum;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class WalletItemServiceTest {

	@MockBean
	WalletItemRepository repository;
	
	@Autowired
	WalletItemService service;
	
	private static final Date DATE = new Date();
	private static final TypeEnum TYPE = TypeEnum.EN;
	private static final String DESCRIPTION = "Conta de Luz";
	private static final BigDecimal VALUE = BigDecimal.valueOf(65);
	
	@Test
	@Order(1)
	public void testSave() {
		BDDMockito.given(repository.save(Mockito.any(WalletItem.class))).willReturn(getMockWalletItem());
		
		WalletItem response = service.save(new WalletItem());
		
		assertNotNull(response);
		assertEquals(DESCRIPTION, response.getDescription());
		assertEquals(0, response.getValue().compareTo(VALUE));
	}
	
	@Test
	@Order(2)
	public void testFindBetweenDates() {
		List<WalletItem> list = new ArrayList<>();
		list.add(getMockWalletItem());
		Page<WalletItem> page = new PageImpl<WalletItem>(list);
		
		BDDMockito.given(repository.findAllByWalletIdAndDateGreaterThanEqualAndDateLessThanEqual(Mockito.anyLong(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.any(PageRequest.class))).willReturn(page);
		
		Page<WalletItem> response = service.findBetweenDates(1L, new Date(), new Date(), 0);
		
		assertNotNull(response);
		assertEquals(1, response.getContent().size());
		assertEquals(DESCRIPTION, response.getContent().get(0).getDescription());
	}
	
	@Test
	@Order(3)
	public void testFindByType() {
		List<WalletItem> list = new ArrayList<>();
		list.add(getMockWalletItem());
		
		BDDMockito.given(repository.findByWalletIdAndType(Mockito.anyLong(), Mockito.any(TypeEnum.class))).willReturn(list);
		
		List<WalletItem> response = service.findByWalletAndType(1L, TypeEnum.EN);
		
		assertNotNull(response);
		assertEquals(TYPE, response.get(0).getType());
	}
	
	@Test
	@Order(4)
	public void testSumByWallet() {
		BigDecimal value = BigDecimal.valueOf(45);
		
		BDDMockito.given(repository.sumByWalletId(Mockito.anyLong())).willReturn(value);
		
		BigDecimal response = service.sumByWalletId(1L);
		
		assertEquals(0, response.compareTo(value));
	}
	
	private WalletItem getMockWalletItem() {
		Wallet w = new Wallet();
		w.setId(1L);
		
		WalletItem wi = new WalletItem(1L, w, DATE, TYPE, DESCRIPTION, VALUE);
		return wi;
	}
}
