package pl.konradboniecki.budget.passwordmanagement.model.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
