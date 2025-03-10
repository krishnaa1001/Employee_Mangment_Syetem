package in.krrishit.controller;

import in.krrishit.model.Client;
import in.krrishit.model.User;
import in.krrishit.repository.ClientRepository;
import in.krrishit.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private UserRepository userRepository; // To create corresponding User record
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // ----- Create Client -----
    @GetMapping("/client/create")
    public String showCreateClientForm(Model model) {
        model.addAttribute("client", new Client());
        return "/client/create_client";
    }
    
    @PostMapping("/client/create")
    public String createClient(@ModelAttribute("client") Client client, RedirectAttributes redirectAttributes) {
        // Generate unique client ID based on current count (formatted as krrishitCliXXX)
        List<Client> clients = clientRepository.findAll();
        int count = clients.size() + 1;
        String clientId = String.format("krrishitCli%03d", count);
        client.setClientId(clientId);
        
        clientRepository.save(client);
        
        // Create corresponding User record if not already existing.
        if(userRepository.findByEmail(client.getEmail()) == null){
            User user = new User();
            user.setEmail(client.getEmail());
            // Default password for login
            String defaultPassword = "password";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setRole("ROLE_CLIENT");
            userRepository.save(user);
        }
        
        // Send notification email to the client.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(client.getEmail());
        message.setSubject("Welcome to KrrishIt Client Portal");
        message.setText("Dear " + client.getCompanyName() + ",\n\n" +
                "Your client account has been created successfully.\n" +
                "Your Client ID is: " + clientId + "\n" +
                "Your Company Name is: " + client.getCompanyName() + "\n" +
                "Your Email is: " + client.getEmail() + "\n" +
                "Your Default Password is: password\n\n" +
                "Please login using the following link:\n" +
                "http://localhost:8080/login\n\n" +
                "After logging in, you will receive an OTP for verification before accessing your dashboard.\n\n" +
                "Regards,\n" +
                "KrrishIt Team");
        mailSender.send(message);
        
        redirectAttributes.addFlashAttribute("success", "Client created successfully with Client ID: " + clientId);
        
        // Redirect to client dashboard after creation (or view page if preferred)
        return "redirect:/dashboard";
    }
    
    // ----- Client Dashboard -----
    @GetMapping("/client_dashboard")
    public String showClientDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        model.addAttribute("email", email);
        return "client/client_dashboard";
    }
    
    // ----- View Clients -----
    @GetMapping("/client/view")
    public String viewClients(@RequestParam(value="search", required=false) String search,
                              @PageableDefault(size = 5) Pageable pageable,
                              Model model) {
        Page<Client> page;
        if (search != null && !search.isEmpty()) {
            page = clientRepository.findByClientIdContainingOrCompanyNameContaining(search, search, pageable);
        } else {
            page = clientRepository.findAll(pageable);
        }
        Map<Long, Boolean> userStatusMap = new HashMap<>();
        page.getContent().forEach(c -> {
            User user = userRepository.findByEmail(c.getEmail());
            userStatusMap.put(c.getId(), (user != null) ? user.isEnabled() : true);
        });
        model.addAttribute("page", page);
        model.addAttribute("userStatusMap", userStatusMap);
        model.addAttribute("search", search);
        return "client/view_clients";
    }

    // ----- Edit Client -----
    @GetMapping("/client/edit/{id}")
    public String showEditClientForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/client/view";
        }
        model.addAttribute("client", client);
        return "client/edit_client";
    }
    
    // ----- Update Client -----
    @PostMapping("/client/update")
    public String updateClient(@ModelAttribute("client") Client client, RedirectAttributes redirectAttributes) {
        Client existingClient = clientRepository.findById(client.getId()).orElse(null);
        if (existingClient == null) {
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/client/view";
        }
        // Update fields
        existingClient.setCompanyName(client.getCompanyName());
        existingClient.setProjectName(client.getProjectName());
        existingClient.setEmail(client.getEmail());
        existingClient.setCountry(client.getCountry());
        existingClient.setCity(client.getCity());
        existingClient.setAddress(client.getAddress());
        clientRepository.save(existingClient);
        
        redirectAttributes.addFlashAttribute("success", "Client updated successfully");
        return "redirect:/client/view";
    }
    
    // ----- Delete Client -----
    @GetMapping("/client/delete/{id}")
    public String deleteClient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/client/view";
        }
        
        // Optionally, delete the corresponding user record if needed
        User user = userRepository.findByEmail(client.getEmail());
        if (user != null) {
            userRepository.delete(user);
        }
        
        clientRepository.delete(client);
        redirectAttributes.addFlashAttribute("success", "Client deleted successfully");
        return "redirect:/client/view";
    }
    // ----- Block Client -----
    @GetMapping("/client/block/{id}")
    public String blockClient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/client/view";
        }
        User user = userRepository.findByEmail(client.getEmail());
        if (user != null) {
            user.setEnabled(false);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Client blocked successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User record not found for this client");
        }
        return "redirect:/client/view";
    }
    
    // ----- Unblock Client -----
    @GetMapping("/client/unblock/{id}")
    public String unblockClient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/client/view";
        }
        User user = userRepository.findByEmail(client.getEmail());
        if (user != null) {
            user.setEnabled(true);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Client unblocked successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User record not found for this client");
        }
        return "redirect:/client/view";
    }
}
