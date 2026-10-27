package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for HelpRequests. */
@Tag(name = "HelpRequests")
@RequestMapping("/api/HelpRequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * This method returns a list of all help requests.
   *
   * @return a list of all help requests
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> helpRequests = helpRequestRepository.findAll();
    return helpRequests;
  }

  /**
   * This method creates a new help request.
   *
   * @param requesterEmail email of the requester
   * @param teamId team id of the requester
   * @param tableOrBreakoutRoom table or breakout room of the requester
   * @param requestTime time of the request
   * @param explanation explanation of the request
   * @param solved whether the request is solved
   * @return the saved help request
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(name = "requestTime")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved) {

    HelpRequest helpRequest = new HelpRequest();

    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setRequestTime(requestTime);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);

    return helpRequestRepository.save(helpRequest);
  }

  /**
   * Get a single help request by id.
   *
   * @param id the id of the help request
   * @return the help request
   */
  @Operation(summary = "Get a single help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {

    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    return helpRequest;
  }

  /**
   * Update a single help request by id.
   *
   * @param id id of the help request to update
   * @param incoming new values for the help request
   * @return the updated help request
   */
  @Operation(
      summary = "Update a single help request",
      description =
          "Updates an existing HelpRequest using its ID and a JSON request body. "
              + "Returns 404 if the ID does not exist.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public HelpRequest updateHelpRequest(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid HelpRequest incoming) {

    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    helpRequest.setRequesterEmail(incoming.getRequesterEmail());
    helpRequest.setTeamId(incoming.getTeamId());
    helpRequest.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
    helpRequest.setRequestTime(incoming.getRequestTime());
    helpRequest.setExplanation(incoming.getExplanation());
    helpRequest.setSolved(incoming.getSolved());

    return helpRequestRepository.save(helpRequest);
  }

  /**
   * Delete a help request by ID.
   *
   * @param id ID of the help request
   * @return confirmation message
   */
  @Operation(
      summary = "Delete a single help request",
      description =
          "Deletes a HelpRequest by ID. Returns 200 with 'record {id} deleted' "
              + "or 404 with 'record {id} not found'.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public ResponseEntity<String> deleteHelpRequest(
      @Parameter(name = "id", description = "ID of the HelpRequest to delete") @RequestParam
          Long id) {

    try {
      helpRequestRepository
          .findById(id)
          .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("record " + id + " not found");
    }

    helpRequestRepository.deleteById(id);

    return ResponseEntity.ok("record " + id + " deleted");
  }
}
