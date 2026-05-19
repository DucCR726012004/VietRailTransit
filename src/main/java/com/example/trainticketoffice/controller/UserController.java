package com.example.trainticketoffice.controller;

import com.example.trainticketoffice.model.User;
import com.example.trainticketoffice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate; // <-- THÊM IMPORT NÀY
import java.util.List;

@Controller
// @RequestMapping("/users") // <-- XÓA DÒNG NÀY
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ===== CHỨC NĂNG REGISTER MỚI =====

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register"; // Trả về file register.html
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") User user,
                                  BindingResult result, Model model,
                                  RedirectAttributes redirectAttributes) {

        // (Chúng ta có thể thêm validation chi tiết hơn ở đây, ví dụ: check trùng email)

        try {
            // Thiết lập các giá trị mặc định cho CUSTOMER
            user.setRole(User.Role.CUSTOMER);
            user.setCreateDate(LocalDate.now());

            boolean isAdded = userService.addUser(user);

            if (!isAdded) {
                // Điều này có thể xảy ra nếu logic addUser của bạn trả về false
                // (Hiện tại, logic của bạn đang kiểm tra email trùng trong service)
                model.addAttribute("errorMessage", "Email này đã được sử dụng. Vui lòng chọn email khác.");
                return "user/register";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login"; // Chuyển về trang login

        } catch (Exception e) {
            // Bắt lỗi nếu email là UNIQUE trong DB (DataIntegrityViolationException)
            model.addAttribute("errorMessage", "Email này đã được sử dụng. Vui lòng chọn email khác.");
            return "user/register";
        }
    }


    // ===== CÁC CHỨC NĂNG ADMIN QUẢN LÝ NGƯỜI DÙNG =====

    @GetMapping("/users")
    public String listUsers(Model model,
                            @RequestParam(value = "keyword", required = false) String keyword) {
        List<User> users = userService.searchUsers(keyword);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalItems", users.size());
        return "user/list";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("roleTypes", new String[]{"STAFF", "CUSTOMER"});
    }

    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id);

        if (user != null) {
            model.addAttribute("user", user);
            addCommonAttributes(model);
            return "user/form";
        }
        return "redirect:/users";
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult result, Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addCommonAttributes(model);
            return "user/form";
        }

        try {
            if (user.getId() == null) {
                boolean isAdded = userService.addUser(user);
                if (!isAdded) {
                    model.addAttribute("errorMessage", "Email đã tồn tại trong hệ thống!");
                    addCommonAttributes(model);
                    return "user/form";
                }
            } else {
                userService.updateUser(user.getId(), user);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin người dùng thành công!");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu thông tin: " + e.getMessage());
            addCommonAttributes(model);
            return "user/form";
        }
    }

    @GetMapping("/users/toggle-lock/{id}")
    public String toggleLockUser(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                userService.toggleLockUser(id);
                String action = user.isLocked() ? "mở khóa" : "khóa";
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã " + action + " tài khoản của " + user.getFullName() + " thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thay đổi trạng thái tài khoản: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng ID " + id + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/users/search")
    public String searchUsersForm() {
        return "user/search";
    }

    @PostMapping("/users/search")
    public String searchUsers(@RequestParam String fullName, Model model) {
        try {
            User user = userService.findByUserName(fullName);
            model.addAttribute("user", user);
            model.addAttribute("searchName", fullName);
            return "user/search";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error searching user: " + e.getMessage());
            return "user/search";
        }
    }
}
