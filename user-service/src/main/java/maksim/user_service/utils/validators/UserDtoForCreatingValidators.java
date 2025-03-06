package maksim.user_service.utils.validators;

import maksim.user_service.models.dtos.UserDtoForCreating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDtoForCreatingValidators {
    private final StringValidators stringValidators;

    @Autowired
    public UserDtoForCreatingValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public boolean isValid(UserDtoForCreating dto) {
        if (isEmpty(dto)) return false;

        screenStringValues(dto);

        return isSafeFromSqlInjection(dto);
    }

    public boolean isEmpty(UserDtoForCreating dto) {
        return dto.getPassword() == null || dto.getEmail() == null || dto.getName() == null;
    }

    public void screenStringValues(UserDtoForCreating dto) {
        dto.setPassword(stringValidators.textScreening(dto.getPassword()));
        dto.setEmail(stringValidators.textScreening(dto.getEmail()));
        dto.setName(stringValidators.textScreening(dto.getName()));
    }

    public boolean isSafeFromSqlInjection(UserDtoForCreating dto) {
        return stringValidators.isSafeFromSqlInjection(dto.getEmail())
                && stringValidators.isSafeFromSqlInjection(dto.getPassword())
                && stringValidators.isSafeFromSqlInjection(dto.getName());
    }

}
