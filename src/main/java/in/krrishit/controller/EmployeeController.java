package in.krrishit.controller;

import in.krrishit.model.Employee;
import in.krrishit.model.User;
import in.krrishit.repository.EmployeeRepository;
import in.krrishit.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private UserRepository userRepository; // For creating corresponding User record
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ----- Create Employee -----
    @GetMapping("/employee/create")
    public String showCreateEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "/employee/create_employee";
    }

    @PostMapping("/employee/create")
    public String createEmployee(@ModelAttribute("employee") Employee employee, RedirectAttributes redirectAttributes) {
        // Generate unique employee ID (e.g., krrishitEmp001)
        List<Employee> employees = employeeRepository.findAll();
        int count = employees.size() + 1;
        String empId = String.format("krrishitEmp%03d", count);
        employee.setEmpId(empId);

        employeeRepository.save(employee);

        // Create corresponding User record if not exists.
        if (userRepository.findByEmail(employee.getEmail()) == null) {
            User user = new User();
            user.setEmail(employee.getEmail());
            String defaultPassword = "password";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setRole("ROLE_EMPLOYEE");
            user.setEnabled(true); // New users are enabled by default
            userRepository.save(user);
        }

        // Send notification email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employee.getEmail());
        message.setSubject("Welcome to KrrishIt Employee Portal");
        message.setText("Dear " + employee.getName() + ",\n\n" +
                "Your employee account has been created successfully.\n" +
                "Your Employee ID is: " + empId + "\n" +
                "Your Email is: " + employee.getEmail() + "\n" +
                "Your Default Password is: password\n\n" +
                "Please use the following link to login and complete the OTP verification process:\n" +
                "http://localhost:8080/login\n\n" +
                "Regards,\nKrrishIt Team");
        mailSender.send(message);

        redirectAttributes.addFlashAttribute("success", "Employee created successfully with Employee ID: " + empId);
        return "redirect:/employee/view";
    }

    // ----- Employee Dashboard (Admin) -----
    @GetMapping("/employee_dashboard")
    public String showEmployeeDashboard(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        return "employee/employee_dashboard";
    }
    
    // ----- View Employees -----
    @GetMapping("/employee/view")
    public String viewEmployees(@RequestParam(value="search", required=false) String search,
                                @PageableDefault(size = 5) Pageable pageable,
                                Model model) {
        Page<Employee> page;
        if (search != null && !search.isEmpty()) {
            page = employeeRepository.findByEmpIdContainingOrNameContaining(search, search, pageable);
        } else {
            page = employeeRepository.findAll(pageable);
        }
        // Build a map of employee id -> user enabled status
        Map<Long, Boolean> userStatusMap = new HashMap<>();
        page.getContent().forEach(emp -> {
            User user = userRepository.findByEmail(emp.getEmail());
            userStatusMap.put(emp.getId(), (user != null) ? user.isEnabled() : true);
        });
        model.addAttribute("page", page);
        model.addAttribute("userStatusMap", userStatusMap);
        model.addAttribute("search", search);
        return "employee/view_employees";
    }
    
    // ----- Edit Employee -----
    @GetMapping("/employee/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
            return "redirect:/employee/view";
        }
        model.addAttribute("employee", employee);
        return "employee/edit_employee";
    }
    
    // ----- Update Employee -----
    @PostMapping("/employee/update")
    public String updateEmployee(@ModelAttribute("employee") Employee employee, RedirectAttributes redirectAttributes) {
        Employee existingEmployee = employeeRepository.findById(employee.getId()).orElse(null);
        if (existingEmployee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
            return "redirect:/employee/view";
        }
        existingEmployee.setName(employee.getName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setCountry(employee.getCountry());
        existingEmployee.setState(employee.getState());
        existingEmployee.setMobile(employee.getMobile());
        existingEmployee.setAddress(employee.getAddress());
        employeeRepository.save(existingEmployee);
        
        redirectAttributes.addFlashAttribute("success", "Employee updated successfully");
        return "redirect:/employee/view";
    }
    
    // ----- Delete Employee -----
    @GetMapping("/employee/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
        } else {
            employeeRepository.delete(employee);
            redirectAttributes.addFlashAttribute("success", "Employee deleted successfully");
        }
        return "redirect:/employee/view";
    }
    
    // ----- Block Employee -----
    @GetMapping("/employee/block/{id}")
    public String blockEmployee(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
            return "redirect:/employee/view";
        }
        User user = userRepository.findByEmail(employee.getEmail());
        if (user != null) {
            user.setEnabled(false); // Block the user from logging in
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Employee blocked successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User record not found for this employee");
        }
        return "redirect:/employee/view";
    }
    
    // ----- Unblock Employee -----
    @GetMapping("/employee/unblock/{id}")
    public String unblockEmployee(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
            return "redirect:/employee/view";
        }
        User user = userRepository.findByEmail(employee.getEmail());
        if (user != null) {
            user.setEnabled(true); // Allow the user to log in again
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Employee unblocked successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User record not found for this employee");
        }
        return "redirect:/employee/view";
    }
}
