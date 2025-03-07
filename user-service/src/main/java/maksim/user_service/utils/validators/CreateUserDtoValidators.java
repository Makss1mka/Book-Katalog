package maksim.user_service.utils.validators;

import maksim.user_service.models.dtos.CreateUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateUserDtoValidators {
    private final StringValidators stringValidators;

    @Autowired
    public CreateUserDtoValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public boolean isValid(CreateUserDto dto) {
        if (isEmpty(dto)) return false;

        screenStringValues(dto);

        return isSafeFromSqlInjection(dto);
    }

    public boolean isEmpty(CreateUserDto dto) {
        return dto.getPassword() == null || dto.getEmail() == null || dto.getName() == null;
    }

    public void screenStringValues(CreateUserDto dto) {
        dto.setPassword(stringValidators.textScreening(dto.getPassword()));
        dto.setEmail(stringValidators.textScreening(dto.getEmail()));
        dto.setName(stringValidators.textScreening(dto.getName()));
    }

    public boolean isSafeFromSqlInjection(CreateUserDto dto) {
        return stringValidators.isSafeFromSqlInjection(dto.getEmail())
                && stringValidators.isSafeFromSqlInjection(dto.getPassword())
                && stringValidators.isSafeFromSqlInjection(dto.getName());
    }

}
