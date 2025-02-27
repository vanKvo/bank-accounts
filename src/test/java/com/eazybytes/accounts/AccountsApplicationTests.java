package com.eazybytes.accounts;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.controller.AccountsController;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.dto.ResponseDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@AutoConfigureMockMvc
class AccountsApplicationTests {

	@MockBean
	private AccountsRepository accountsRepository;
	@MockBean
	private CustomerRepository customerRepository;
	@Autowired
	private IAccountsService iAccountsService;
	@Autowired
	private AccountsController accountsController;

	private AccountsDto accountsDto;
	private CustomerDto customerDto;
	private Customer customer;
	private Accounts account;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		accountsDto = new AccountsDto();
		accountsDto.setAccountNumber(1122334455L);
		accountsDto.setAccountType("Checking");
		accountsDto.setBranchAddress("123 ABC");
		customerDto = new CustomerDto();
		customerDto.setAccountsDto(accountsDto);
		customerDto.setName("John Hans");
		customerDto.setEmail("test@gmail.com");
		customerDto.setMobileNumber("5717778989");

		customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
		account = AccountsMapper.mapToAccounts(accountsDto, new Accounts());
	}

	/** Tests for the service layer */

	@Test
	public void fetchAccountByMobileNumber_returnAccount() throws Exception {

		when(customerRepository.findByMobileNumber("5717778989")).thenReturn(Optional.of(customer));
		when(accountsRepository.findByCustomerId(customer.getCustomerId())).thenReturn(Optional.of(account));

		CustomerDto customerDto = iAccountsService.fetchAccount("5717778989");

		assertEquals(customer.getMobileNumber(), customerDto.getMobileNumber());
		assertEquals(account.getAccountNumber(), customerDto.getAccountsDto().getAccountNumber());
	}

	@Test
	public void fetchAccountByMobileNumber_returnNoAccount() throws Exception {

		when(customerRepository.findByMobileNumber("5717778888"))
				               .thenThrow(new ResourceNotFoundException("Customer", "mobileNumber", "5717778888"));

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> iAccountsService.fetchAccount("5717778888"));
		assertEquals("Customer not found with the given input data mobileNumber : '5717778888'", exception.getMessage());
	}

	@Test
	public void createAccount_success() throws Exception {
		when(customerRepository.findByMobileNumber(customer.getMobileNumber())).thenReturn(Optional.empty());
		when(customerRepository.save(any(Customer.class))).thenReturn(customer);
		when(accountsRepository.save(any(Accounts.class))).thenReturn(account);

		iAccountsService.createAccount(customerDto);

		verify(customerRepository, times(1)).findByMobileNumber(customer.getMobileNumber());
		verify(customerRepository, times(1)).save(any(Customer.class));
		verify(accountsRepository, times(1)).save(any(Accounts.class));
	}

	// Have issue of exception is not thrown
	@Test
	public void createAccount_customerExist_throwsException() throws Exception {
		doThrow(new CustomerAlreadyExistsException("Customer already registered with given mobileNumber 5717778989"))
				.when(customerRepository).findByMobileNumber(customerDto.getMobileNumber());

		CustomerAlreadyExistsException ex = assertThrows(CustomerAlreadyExistsException.class,
				() -> iAccountsService.createAccount(customerDto));
		assertEquals("Customer already registered with given mobileNumber "
				+customerDto.getMobileNumber(), ex.getMessage());

		// Verify that the repositories were not called (since the exception was thrown early)
		verify(customerRepository, never()).save(any(Customer.class));
		verify(accountsRepository, never()).save(any(Accounts.class));
	}

	@Test
	public void updateAccount_success() throws Exception {
		account.setCustomerId(2L);

		when(accountsRepository.findById(accountsDto.getAccountNumber())).thenReturn(Optional.of(account));
		when(accountsRepository.save(any(Accounts.class))).thenReturn(account);
		when(customerRepository.findById(account.getCustomerId())).thenReturn(Optional.of(customer));
		when(customerRepository.save(any(Customer.class))).thenReturn(customer);

		Boolean isUpdated = iAccountsService.updateAccount(customerDto);
		assertTrue(isUpdated);
	}

}
