package com.eazybytes.accounts;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountsControllerTests {

    @MockBean
    IAccountsService iAccountsService;
    @MockBean
    private AccountsRepository accountsRepository;
    @MockBean
    private CustomerRepository customerRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private AccountsDto accountsDto;
    private CustomerDto customerDto;
    Customer customer;
    Accounts account;

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
        account.setCustomerId(1L);
    }

    @Test
    public void fetchAccountByMobileNumber_ok() throws Exception {
        when(iAccountsService.fetchAccount("5717778989")).thenReturn(customerDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/fetch?mobileNumber=5717778989")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(customerDto)));
    }

    @Test
    public void fetchAccountByMobileNumber_notFound() throws Exception {
        when(iAccountsService.fetchAccount(customerDto.getMobileNumber())).thenThrow(new ResourceNotFoundException("Customer", "mobileNumber", customerDto.getMobileNumber()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/fetch?mobileNumber="+customerDto.getMobileNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createAccount_created() throws Exception {
        doNothing().when(iAccountsService).createAccount(any(CustomerDto.class));
        String customerDtoJsonStr = objectMapper.writeValueAsString(customerDto);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/create?mobileNumber=5717778989")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(customerDtoJsonStr);

        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated());
    }

    @Test
    public void createAccount_customerExistsException() throws Exception {
        doThrow(new CustomerAlreadyExistsException("Customer already registered with given mobileNumber 5717778989"))
                .when(iAccountsService).createAccount(any(CustomerDto.class));
        String customerDtoJsonStr = objectMapper.writeValueAsString(customerDto);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/create?mobileNumber=5717778989")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(customerDtoJsonStr);

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAccount_ok() throws Exception {
        customerDto.setEmail("updatedTestEmail@gmail.com");
        when(iAccountsService.updateAccount(customerDto)).thenReturn(true);

        String  updatedCustomer = objectMapper.writeValueAsString(customerDto);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/api/update")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updatedCustomer);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void updateAccount_failed() throws Exception {
        customerDto.setEmail("updatedTestEmail@gmail.com");
        when(iAccountsService.updateAccount(customerDto)).thenReturn(false);

        String  updatedCustomer = objectMapper.writeValueAsString(customerDto);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/api/update")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updatedCustomer);

        mockMvc.perform(mockRequest)
                .andExpect(status().isExpectationFailed());
    }

    @Test
    public void deleteAccount_ok() throws Exception {
        when(iAccountsService.deleteAccount(customerDto.getMobileNumber())).thenReturn(true);
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/api/delete?mobileNumber=" + customerDto.getMobileNumber())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAccount_failed() throws Exception {
        when(iAccountsService.deleteAccount(customerDto.getMobileNumber())).thenReturn(false);
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/api/delete?mobileNumber=" + customerDto.getMobileNumber())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andExpect(status().isExpectationFailed());
    }


}
