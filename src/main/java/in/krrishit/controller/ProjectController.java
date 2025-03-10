package in.krrishit.controller;

import in.krrishit.model.Project;
import in.krrishit.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    // ----- Create Project -----
    @GetMapping("/project/create")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "project/create_project";
    }

    @PostMapping("/project/create")
    public String createProject(@ModelAttribute("project") Project project, RedirectAttributes redirectAttributes) {
        // Generate unique project ID based on current count (formatted as krrishitProjXXX)
        List<Project> projects = projectRepository.findAll();
        int count = projects.size() + 1;
        String projectId = String.format("krrishitProj%03d", count);
        project.setProjectId(projectId);

        projectRepository.save(project);
        redirectAttributes.addFlashAttribute("success", "Project created successfully with Project ID: " + projectId);
        return "redirect:/project/view";
    }

    // ----- View Projects -----
    @GetMapping("/project/view")
    public String viewProjects(@RequestParam(value="search", required=false) String search,
                               @PageableDefault(size = 5) Pageable pageable,
                               Model model) {
        Page<Project> page;
        if (search != null && !search.isEmpty()) {
            page = projectRepository.findByProjectIdContainingOrProjectNameContaining(search, search, pageable);
        } else {
            page = projectRepository.findAll(pageable);
        }
        model.addAttribute("page", page);
        model.addAttribute("search", search);
        return "project/view_projects";
    }
    // ----- Edit Project -----
    @GetMapping("/project/edit/{id}")
    public String showEditProjectForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            redirectAttributes.addFlashAttribute("error", "Project not found");
            return "redirect:/project/view";
        }
        model.addAttribute("project", project);
        return "project/edit_project";
    }

    // ----- Update Project -----
    @PostMapping("/project/update")
    public String updateProject(@ModelAttribute("project") Project project, RedirectAttributes redirectAttributes) {
        Project existingProject = projectRepository.findById(project.getId()).orElse(null);
        if (existingProject == null) {
            redirectAttributes.addFlashAttribute("error", "Project not found");
            return "redirect:/project/view";
        }
        // Update fields
        existingProject.setProjectName(project.getProjectName());
        existingProject.setCompanyName(project.getCompanyName());
        existingProject.setCompanyEmail(project.getCompanyEmail());
        existingProject.setCompanyAddress(project.getCompanyAddress());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setDeadlineDate(project.getDeadlineDate());
        projectRepository.save(existingProject);
        redirectAttributes.addFlashAttribute("success", "Project updated successfully");
        return "redirect:/project/view";
    }

    // ----- Delete Project -----
    @GetMapping("/project/delete/{id}")
    public String deleteProject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            redirectAttributes.addFlashAttribute("error", "Project not found");
        } else {
            projectRepository.delete(project);
            redirectAttributes.addFlashAttribute("success", "Project deleted successfully");
        }
        return "redirect:/project/view";
    }
}
