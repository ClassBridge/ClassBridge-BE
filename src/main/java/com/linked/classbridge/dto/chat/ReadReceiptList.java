package com.linked.classbridge.dto.chat;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadReceiptList {

    private List<ReadReceipt> readReceipts;

    @Override
    public int hashCode() {
        return Objects.hash(readReceipts);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReadReceiptList that = (ReadReceiptList) obj;
        return Objects.equals(readReceipts, that.readReceipts);
    }
}
