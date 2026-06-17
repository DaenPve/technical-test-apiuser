package com.chakraytest.apiusers.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.chakraytest.apiusers.dto.LoginDto;
import com.chakraytest.apiusers.dto.UserDto;
import com.chakraytest.apiusers.model.Address;
import com.chakraytest.apiusers.model.User;
import com.chakraytest.apiusers.service.EncryptionService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicInteger addresId = new AtomicInteger(1);
    private final EncryptionService encryptionService;

    public UserController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    // RUTA GET
    @GetMapping("/users")
    public List<User> getUsers(
            @RequestParam(required = false) String sortedBy,
            @RequestParam(required = false) String filter) {

        List<User> result = new ArrayList<>(users);

        // verificamos parametros y si aplican.

        if (filter != null && !filter.isBlank()) {
            result = applyFilter(result, filter);
        }

        if (sortedBy != null && !sortedBy.isBlank()) {
            result = applySorting(result, sortedBy);
        }

        return result;

    }

    // RUTA POST
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDto loginDto) {
        User user = users.stream().filter(us -> us.getTax_id().equalsIgnoreCase(loginDto.getTax_id())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email or Password incorrect"));
        String encryptedPassword = encryptionService.encrypt(loginDto.getPassword());

        if (!user.getPassword().equals(encryptedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password incorrect");
        }

        return Map.of(
                "message", "Login successful",
                "tax_id", user.getTax_id());
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserDto userDto) {

        if (userDto.getTax_id() == null || userDto.getTax_id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax ID required");
        }

        boolean taxIdExists = users.stream().anyMatch(user -> user.getTax_id().equalsIgnoreCase(userDto.getTax_id()));

        if (taxIdExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax ID already exists");
        }

        String createdAt = ZonedDateTime.now(ZoneId.of("Indian/Antananarivo"))
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        // Instanciamos direcciones
        List<Address> addresses = userDto.getAddresses().stream().map(addressDto -> new Address(
                addresId.getAndIncrement(),
                addressDto.getName(),
                addressDto.getStreet(),
                addressDto.getCountry_code())).toList();

        // Instaciamos usuario.
        User user = new User(
                UUID.randomUUID(),
                userDto.getEmail(),
                userDto.getName(),
                userDto.getPhone(),
                encryptionService.encrypt(userDto.getPassword()),
                userDto.getTax_id().toUpperCase(),
                createdAt,
                addresses);

        users.add(user);

        return user;

    }

    @PatchMapping("/users/{id}")
    public User updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {

        // Buscamos usuario
        User user = users.stream().filter(u -> u.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        if (userDto.getPhone() != null) {
            user.setPhone(userDto.getPhone());
        }

        if (userDto.getPassword() != null) {
            user.setPassword(encryptionService.encrypt(userDto.getPassword()));
        }

        if (userDto.getTax_id() != null) {

            if(userDto.getTax_id().isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax ID required");
            }
            
            boolean taxIdExists = users.stream()
                    .anyMatch(u -> u.getTax_id().equalsIgnoreCase(userDto.getTax_id()) && !u.getId().equals(id));

            if (taxIdExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax ID already exists");
            }

            user.setTax_id(userDto.getTax_id().toUpperCase());
        }

        if (userDto.getAddresses() != null && !userDto.getAddresses().isEmpty()) {
            List<Address> addresses = userDto.getAddresses().stream().map(addressDto -> new Address(
                    addresId.getAndIncrement(),
                    addressDto.getName(),
                    addressDto.getStreet(),
                    addressDto.getCountry_code())).toList();

            user.setAddresses(addresses);
        }

        return user;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable UUID id) {
        User user = users.stream().filter(u -> u.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        users.remove(user);
        return Map.of("message", "User deleted successfully");
    }

    private List<User> applySorting(List<User> users, String sortedBy) {
        return users.stream().sorted(getComparator(sortedBy)).toList();
    }

    private Comparator<User> getComparator(String sortedBy) {
        return switch (sortedBy) {
            case "id" -> Comparator.comparing(user -> user.getId());
            case "email" -> Comparator.comparing(user -> user.getEmail());
            case "name" -> Comparator.comparing(user -> user.getName());
            case "phone" -> Comparator.comparing(user -> user.getPhone());
            case "tax_id" -> Comparator.comparing(user -> user.getTax_id());
            case "created_at" -> Comparator.comparing(user -> user.getCreated_at());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        };
    }

    private List<User> applyFilter(List<User> users, String filter) {
        String[] parts = filter.split("\\s+|\\+", 3);

        if (parts.length != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        String attribute = parts[0];
        String operator = parts[1];
        String value = parts[2];

        return users.stream().filter(user -> matchesFilter(user, attribute, operator, value)).toList();
    }

    private boolean matchesFilter(User user, String attribute, String operator, String value) {
        String fieldValue = getUserFieldValue(user, attribute);

        if (fieldValue == null) {
            return false;
        }

        String field = fieldValue.toLowerCase();
        String compareValue = value.toLowerCase();

        return switch (operator) {
            case "eq" -> field.equals(compareValue);
            case "co" -> field.contains(compareValue);
            case "sw" -> field.startsWith(compareValue);
            case "ew" -> field.endsWith(compareValue);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filter");
        };
    }

    private String getUserFieldValue(User user, String attribute) {
        return switch (attribute) {
            case "id" -> user.getId().toString();
            case "email" -> user.getEmail();
            case "name" -> user.getName();
            case "phone" -> user.getPhone();
            case "tax_id" -> user.getTax_id();
            case "created_at" -> user.getCreated_at();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filter");
        };
    }

}
