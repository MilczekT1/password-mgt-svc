package pl.konradboniecki.budget.passwordmanagement.model.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Data
public class ActivationLinkRequest implements Serializable {

    private Account account;
    private String resetCode;

    public ObjectNode prepareJsonForMail() {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode accNode = objectMapper.createObjectNode();
        accNode.put("id", getAccount().getId());
        accNode.put("firstName", getAccount().getFirstName());
        accNode.put("lastName", getAccount().getLastName());
        accNode.put("email", getAccount().getEmail());

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("Account", accNode);
        rootNode.put("ResetCode", getResetCode());
        return rootNode;
    }
}
