package com.chakraytest.apiusers.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.chakraytest.apiusers.dto.AddressDto;
import com.chakraytest.apiusers.dto.LoginDto;
import com.chakraytest.apiusers.dto.UserDto;
import com.chakraytest.apiusers.model.User;
import com.chakraytest.apiusers.service.EncryptionService;

public class UserControllerTest {
    private EncryptionService encryptionService;
    private UserController uController;


    @BeforeEach
    void setUp() {
        encryptionService = mock(EncryptionService.class);
        uController = new UserController(encryptionService);
    }

    @Test
    void createUser_success() {
        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611696303", "testpwddavid", "XXXX010203ABC");

        User user = uController.createUser(userDto);

        assertNotNull(user.getId());
        assertEquals("deiv@email.com", user.getEmail());
        assertEquals("david", user.getName());
        assertEquals("+524611696303", user.getPhone());
        assertEquals("encrypted-password", user.getPassword());
        assertEquals("XXXX010203ABC", user.getTax_id());
        assertNotNull(user.getCreated_at());
        assertEquals(1, user.getAddresses().size());

        verify(encryptionService).encrypt("testpwddavid");

    }

    @Test
    void createUser_taxId_duplicated() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611696303", "testpwddavid", "XXXX010203ABC");

        uController.createUser(userDto);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> uController.createUser(userDto));

        assertEquals(400, ex.getStatusCode().value());

        assertEquals("Tax ID already exists", ex.getReason());

    }

    @Test
    void login_success() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611696303", "testpwddavid", "XXXX010203ABC");

        uController.createUser(userDto);

        LoginDto loginDto = new LoginDto();
        loginDto.setTax_id("XXXX010203ABC");
        loginDto.setPassword("testpwddavid");

        Map<String, Object> response = uController.login(loginDto);

        assertEquals("Login successful", response.get("message"));
        assertEquals("XXXX010203ABC", response.get("tax_id"));

    }

    @Test
    void login_unathorized() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");
        when(encryptionService.encrypt("anothertestpwd")).thenReturn("wrong-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611696303", "testpwddavid", "XXXX010203ABC");

        uController.createUser(userDto);

        LoginDto loginDto = new LoginDto();
        loginDto.setTax_id("XXXX010203ABC");
        loginDto.setPassword("anothertestpwd");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> uController.login(loginDto));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Email or Password incorrect", ex.getReason());

    }

    @Test
    void login_invalid_tax() {

        LoginDto loginDto = new LoginDto();
        loginDto.setTax_id("XXXX000111ABC");
        loginDto.setPassword("anothertestpwd");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> uController.login(loginDto));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Email or Password incorrect", ex.getReason());

    }

    @Test
    void getUsers_sorted_by_name() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto(
                "b@email.com", "b", "+524611111111", "testpwdb", "XXXX011111BBB");

        UserDto userDto2 = setUserDto(
                "g@email.com", "g", "+524612222222", "testpwdg", "XXXX022222GGG");

        uController.createUser(userDto);
        uController.createUser(userDto2);

        List<User> users = uController.getUsers("name", null);

        assertEquals(2, users.size());
        assertEquals("b", users.get(0).getName());
        assertEquals("g", users.get(1).getName());

    }

    @Test
    void getUsers_filter_by_email() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto(
                "b@email.com", "b", "+524611111111", "testpwdb", "XXXX011111BBB");

        UserDto userDto2 = setUserDto(
                "g@hotmail.com", "g", "+524612222222", "testpwdg", "XXXX022222GGG");

        uController.createUser(userDto);
        uController.createUser(userDto2);

        List<User> users = uController.getUsers(null, "email co email");

        assertEquals(1, users.size());
        assertEquals("b@email.com", users.get(0).getEmail());
    }

    @Test
    void getUsers_sorted_invalid() {

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> uController.getUsers("monica", null));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Invalid parameter", ex.getReason());

    }

    @Test
    void updateUser_successful() {

        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");
        when(encryptionService.encrypt("newpwdtest")).thenReturn("new-encrypted-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611233221", "testpwddavid", "XXXX010203ABC");

        User user = uController.createUser(userDto);

        UserDto uDto = new UserDto();
        uDto.setName("david2");
        uDto.setPassword("newpwdtest");

        User updatedUser = uController.updateUser(user.getId(), uDto);

        assertEquals("david2", updatedUser.getName());
        assertEquals("new-encrypted-password", updatedUser.getPassword());

    }

    @Test
    void deleteUser_successful() {
        when(encryptionService.encrypt("testpwddavid")).thenReturn("encrypted-password");

        UserDto userDto = setUserDto("deiv@email.com", "david", "+524611233221", "testpwddavid", "XXXX010203ABC");

        User user = uController.createUser(userDto);

        Map<String, String> result = uController.deleteUser(user.getId());

        assertEquals("User deleted successfully", result.get("message"));

        List<User> users = uController.getUsers(null, null);

        assertEquals(0, users.size());

    }

    private UserDto setUserDto(
            String email,
            String name,
            String phone,
            String password,
            String tax_id) {
        AddressDto addrDto = new AddressDto();
        addrDto.setName("Casa");
        addrDto.setStreet("Test #123");
        addrDto.setCountry_code("MX");

        UserDto userDto = new UserDto(email, name, phone, password, tax_id,
                List.of(addrDto));

        return userDto;
    }
}
