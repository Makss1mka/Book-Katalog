package maksim.userservice.utils.validators;

import maksim.userservice.models.dtos.UpdateUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserDtoValidators {
    private final StringValidators stringValidators;

    @Autowired
    public UpdateUserDtoValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public boolean isValid(UpdateUserDto dto) {
        if (isEmpty(dto)) {
            return false;
        }

        screenStringValues(dto);

        return isSafeFromSqlInjection(dto);
    }

    public boolean isEmpty(UpdateUserDto dto) {
        return dto.getNewEmail() == null || dto.getNewPassword() == null || dto.getNewName() == null;
    }

    public void screenStringValues(UpdateUserDto dto) {
        dto.setNewPassword(stringValidators.textScreening(dto.getNewPassword()));
        dto.setNewEmail(stringValidators.textScreening(dto.getNewEmail()));
        dto.setNewEmail(stringValidators.textScreening(dto.getNewEmail()));
    }

    public boolean isSafeFromSqlInjection(UpdateUserDto dto) {
        return stringValidators.isSafeFromSqlInjection(dto.getNewEmail())
            && stringValidators.isSafeFromSqlInjection(dto.getNewEmail())
            && stringValidators.isSafeFromSqlInjection(dto.getNewEmail());
    }

}
