import { z } from "zod";
import dateSchema from "./date-schema";

const AccountSchema = z.object({
  id: z.string(),
  createdAt: dateSchema,
  updatedAt: dateSchema,
});

type Account = z.infer<typeof AccountSchema>;

export default Account
export {AccountSchema};