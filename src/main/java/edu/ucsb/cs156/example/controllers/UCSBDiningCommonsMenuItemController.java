package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  // GET all menu items

  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {

    Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();

    return items;
  }

  // POST a new menu item

  @Operation(summary = "Create a new UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station) {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();

    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemRepository.save(item);

    return savedItem;
  }

  // GET a single menu item

  @Operation(
      summary = "Get a single UCSB Dining Commons Menu Item",
      description = "Retrieve a menu item by its ID. Returns 404 if the ID does not exist.")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(
      @Parameter(name = "id", description = "The ID of the menu item") @RequestParam Long id) {

    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));
    return item;
  }

  // Update a single Dining Commons menu item
  @Operation(summary = "Update a single Dining Commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    menuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    menuItem.setName(incoming.getName());
    menuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return menuItem;
  }
}
